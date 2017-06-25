package org.springframework.data.arangodb.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.List;

public class ArangoParameters extends Parameters<ArangoParameters, ArangoParameters.ArangoParameter> {
	public ArangoParameters(Method method) {
		super(method);
	}

	public ArangoParameters(List<ArangoParameter> originals) {
		super(originals);
	}

	@Override
	protected ArangoParameter createParameter(MethodParameter methodParameter) {
		return new ArangoParameter(methodParameter);
	}

	@Override
	protected ArangoParameters createFrom(List<ArangoParameter> list) {
		return new ArangoParameters(list);
	}

	static class ArangoParameter extends Parameter {
		public ArangoParameter(MethodParameter parameter) {
			super(parameter);
		}
	}

	@Override
	public ArangoParameters getBindableParameters() {
		return super.getBindableParameters();
	}

	@Override
	public ArangoParameter getParameter(int index) {
		return super.getParameter(index);
	}
}
