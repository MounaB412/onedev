package io.onedev.server.product;

import io.onedev.server.git.GitFilter;
import io.onedev.server.git.hookcallback.GitPostReceiveCallback;
import io.onedev.server.git.hookcallback.GitPreReceiveCallback;

public class ProductServletGitParameters {
	public GitFilter gitFilter;
	public GitPreReceiveCallback preReceiveServlet;
	public GitPostReceiveCallback postReceiveServlet;

	public ProductServletGitParameters(GitFilter gitFilter, GitPreReceiveCallback preReceiveServlet,
			GitPostReceiveCallback postReceiveServlet) {
		this.gitFilter = gitFilter;
		this.preReceiveServlet = preReceiveServlet;
		this.postReceiveServlet = postReceiveServlet;
	}
}