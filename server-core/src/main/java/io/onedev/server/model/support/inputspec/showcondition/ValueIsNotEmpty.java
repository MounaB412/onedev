package io.onedev.server.model.support.inputspec.showcondition;

import java.util.Collection;
import java.util.List;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name="is not empty")
public class ValueIsNotEmpty implements ValueMatcher {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean matches(List<String> values) {
		return !values.isEmpty();
	}

	public void getUndefinedFieldValues(ShowCondition showCondition, SpecifiedChoices specifiedChoices,
			Collection<UndefinedFieldValue> undefinedFieldValues, FieldSpec field, FieldSpec fieldSpec) {
	}
	
}
