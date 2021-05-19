package org.eclipse.epsilon.egl.sync;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
import org.eclipse.epsilon.eol.models.IModel;
//comprae between all the types 
public class DataTypes {

	public boolean isCompatibale(String sFile, String sModel) {
		Object s = getType(sFile);
		Object m = getType(sModel);
		return s.getClass() == m.getClass();
	}

	public Object getType(String s) {
		// Long Integer
		Pattern pLong = Pattern.compile("\\d+");
		// Float Double
		Pattern pDouble = Pattern.compile("\\d+\\.*\\,*");
		Matcher mLong = pLong.matcher(s);
		Matcher mDouble = pDouble.matcher(s);
		// Boolean
		if (s.equals("true") || s.equals("false"))
			return Boolean.parseBoolean(s);
		else if (mLong.matches()) {
			return Long.parseLong(s);
		} else if (mDouble.matches()) {
			return Double.parseDouble(s);
		} else
			return s;
	}

	public static void getModelValue(IModel model, String id, String attribute, int type, ArrayList<String> values, int n) {
		Object modelElement1 = model.getElementById(id);
		IPropertySetter propertySetter = model.getPropertySetter();
		propertySetter.setObject(modelElement1);
		propertySetter.setProperty(attribute);
		try {
			String v = values.get(n);
			Object converted = null;
			switch (type) {
			case 0: converted = v; break;
			case 1: converted = Integer.parseInt(v); break;
			case 2: converted = Double.parseDouble(v); break;
			case 3: converted = Float.parseFloat(v); break;
			case 4: converted = Boolean.parseBoolean(v); break;
			case 5: converted = Long.parseLong(v); break;
			case 6: converted = Short.parseShort(v); break;
			case 7: converted = Byte.parseByte(v); break;
			}
			propertySetter.invoke(converted);
		} catch (EolRuntimeException e) {
			e.printStackTrace();
		}
		model.store();
	}
}