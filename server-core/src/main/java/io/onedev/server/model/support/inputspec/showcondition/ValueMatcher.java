package io.onedev.server.model.support.inputspec.showcondition;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueMatcher extends Serializable {
	
	boolean matches(List<String> values);

	public abstract void getUndefinedFieldValues(ShowCondition showCondition, SpecifiedChoices specifiedChoices,
			Collection<UndefinedFieldValue> undefinedFieldValues, FieldSpec field, FieldSpec fieldSpec);
	
}
