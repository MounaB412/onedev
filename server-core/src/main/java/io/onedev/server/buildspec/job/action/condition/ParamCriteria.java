package io.onedev.server.buildspec.job.action.condition;

import java.util.List;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class ParamCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		return paramValues != null && paramValues.contains(value);
	}

	@Override
	public String asString() {
		return quote(name) + " " 
				+ ActionCondition.getRuleName(ActionConditionLexer.Is) + " "
				+ quote(value);
	}
	
}