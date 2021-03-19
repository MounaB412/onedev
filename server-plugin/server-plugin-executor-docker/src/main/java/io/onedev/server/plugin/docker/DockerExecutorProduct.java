package io.onedev.server.plugin.docker;


import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.util.SimpleLogger;
import java.util.concurrent.atomic.AtomicReference;
import io.onedev.commons.utils.command.LineConsumer;
import com.google.common.base.Preconditions;
import io.onedev.server.buildspec.job.JobService;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.commons.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.ExplicitException;
import java.io.Serializable;

public class DockerExecutorProduct implements Serializable {
	private String dockerExecutable;

	public String getDockerExecutable() {
		return dockerExecutable;
	}

	public void setDockerExecutable(String dockerExecutable) {
		this.dockerExecutable = dockerExecutable;
	}

	public Commandline newDocker() {
		if (dockerExecutable != null)
			return new Commandline(dockerExecutable);
		else
			return new Commandline("docker");
	}

	public String getImageOS(SimpleLogger jobLogger, String image) {
		Commandline docker = newDocker();
		docker.addArgs("image", "inspect", "-f", "{{.Os}}", image);
		AtomicReference<String> osRef = new AtomicReference<>(null);
		docker.execute(new LineConsumer() {
			@Override
			public void consume(String line) {
				osRef.set(line);
			}
		}, new LineConsumer() {
			@Override
			public void consume(String line) {
				jobLogger.log(line);
			}
		}).checkReturnCode();
		return Preconditions.checkNotNull(osRef.get());
	}

	@SuppressWarnings("resource")
	public void startService(String network, JobService jobService, SimpleLogger jobLogger) {
		jobLogger.log("Pulling service image...");
		Commandline docker = newDocker();
		docker.addArgs("pull", jobService.getImage());
		docker.execute(new LineConsumer() {
			@Override
			public void consume(String line) {
				DockerExecutor.logger.debug(line);
			}
		}, new LineConsumer() {
			@Override
			public void consume(String line) {
				jobLogger.log(line);
			}
		}).checkReturnCode();
		jobLogger.log("Creating service container...");
		String containerName = network + "-service-" + jobService.getName();
		docker.clearArgs();
		docker.addArgs("run", "-d", "--name=" + containerName, "--network=" + network,
				"--network-alias=" + jobService.getName());
		for (EnvVar var : jobService.getEnvVars())
			docker.addArgs("--env", var.getName() + "=" + var.getValue());
		docker.addArgs(jobService.getImage());
		if (jobService.getArguments() != null) {
			for (String token : StringUtils.parseQuoteTokens(jobService.getArguments()))
				docker.addArgs(token);
		}
		docker.execute(new LineConsumer() {
			@Override
			public void consume(String line) {
			}
		}, new LineConsumer() {
			@Override
			public void consume(String line) {
				jobLogger.log(line);
			}
		}).checkReturnCode();
		jobLogger.log("Waiting for service to be ready...");
		boolean isWindows = getImageOS(jobLogger, jobService.getImage()).equalsIgnoreCase("windows");
		ObjectMapper jsonReader = OneDev.getInstance(ObjectMapper.class);
		while (true) {
			StringBuilder builder = new StringBuilder();
			docker.clearArgs();
			docker.addArgs("inspect", containerName);
			docker.execute(new LineConsumer(StandardCharsets.UTF_8.name()) {
				@Override
				public void consume(String line) {
					builder.append(line).append("\n");
				}
			}, new LineConsumer() {
				@Override
				public void consume(String line) {
					jobLogger.log(line);
				}
			}).checkReturnCode();
			JsonNode stateNode;
			try {
				stateNode = jsonReader.readTree(builder.toString()).iterator().next().get("State");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (stateNode.get("Status").asText().equals("running")) {
				docker.clearArgs();
				docker.addArgs("exec", containerName);
				if (isWindows)
					docker.addArgs("cmd", "/c", jobService.getReadinessCheckCommand());
				else
					docker.addArgs("sh", "-c", jobService.getReadinessCheckCommand());
				ExecutionResult result = docker.execute(new LineConsumer() {
					@Override
					public void consume(String line) {
						jobLogger.log("Service readiness check: " + line);
					}
				}, new LineConsumer() {
					@Override
					public void consume(String line) {
						jobLogger.log("Service readiness check: " + line);
					}
				});
				if (result.getReturnCode() == 0) {
					jobLogger.log("Service is ready");
					break;
				}
			} else if (stateNode.get("Status").asText().equals("exited")) {
				if (stateNode.get("OOMKilled").asText().equals("true"))
					jobLogger.log("Out of memory");
				else if (stateNode.get("Error").asText().length() != 0)
					jobLogger.log(stateNode.get("Error").asText());
				docker.clearArgs();
				docker.addArgs("logs", containerName);
				docker.execute(new LineConsumer(StandardCharsets.UTF_8.name()) {
					@Override
					public void consume(String line) {
						jobLogger.log(line);
					}
				}, new LineConsumer(StandardCharsets.UTF_8.name()) {
					@Override
					public void consume(String line) {
						jobLogger.log(line);
					}
				}).checkReturnCode();
				throw new ExplicitException(
						String.format("Service '" + jobService.getName() + "' is stopped unexpectedly"));
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}