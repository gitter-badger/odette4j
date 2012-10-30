/**
 * 
 */
package com.de.grossmann.carthago.protocol.odette.codec.data.command;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import com.de.grossmann.carthago.protocol.odette.codec.data.OFTPType;

/**
 * @author Micha
 *
 */
public abstract class Command
{
	private static final String CODEPAGE = "ASCII";

	public final byte[] getBytes()
	{
		Field[] fields = this.getClass().getDeclaredFields();
		Arrays.sort(fields, new OFTPFieldComparator());
		ByteBuffer byteBuffer = ByteBuffer.allocate(0);
		
		for (int ii = 0; ii < fields.length; ii++)
		{
			Field field = fields[ii];
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (int jj = 0; jj < annotations.length; jj++)
			{
				Annotation annotation = annotations[jj];
				if (annotation.annotationType().isAssignableFrom(OFTPType.class))
				{
					OFTPType type = (OFTPType) annotation;
					ByteBuffer newByteBuffer = ByteBuffer.allocate(type.length() + byteBuffer.capacity());
					newByteBuffer.put(byteBuffer.array());
					newByteBuffer.put(this.getBytesFromField(field, type));
					byteBuffer = newByteBuffer;
				}
			}
		}
		return byteBuffer.array();
	}

	protected final void setBytes(byte[] byteArray)
	{
		Field[] fields = this.getClass().getDeclaredFields();
		Arrays.sort(fields, new OFTPFieldComparator());
		
		for (int ii = 0; ii < fields.length; ii++)
		{
			Field field = fields[ii];
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (int jj = 0; jj < annotations.length; jj++)
			{
				Annotation annotation = annotations[jj];
				if (annotation.annotationType().isAssignableFrom(OFTPType.class))
				{
					OFTPType oftpType = (OFTPType) annotation;
					this.setFieldByBytes(field, byteArray, oftpType);
				}
			}
		}
	}
	
	private final void setFieldByBytes(Field field, final byte[] byteArray, final OFTPType oftpType)
	{
		field.setAccessible(true);
		try
		{
			if ((oftpType.position() < byteArray.length) && ((oftpType.position() + oftpType.length()) <= byteArray.length))
			{
				if (field.getType().isAssignableFrom(String.class))
				{
					this.setStringByBytes(field, byteArray, oftpType);
				}
				else if (field.getType().isAssignableFrom(Integer.class))
				{
					this.setIntegerByBytes(field, byteArray, oftpType);
				}
				else if (field.getType().isAssignableFrom(Long.class))
				{
					this.setLongByBytes(field, byteArray, oftpType);
				}
				else if (field.getType().isAssignableFrom(BigInteger.class))
				{
					this.setBigIntegerByBytes(field, byteArray, oftpType);
				}
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	private final void setStringByBytes(Field field, final byte[] byteArray, final OFTPType oftpType)
	throws UnsupportedEncodingException, IllegalArgumentException, IllegalAccessException
	{
		String newValue;
		newValue = new String(byteArray, oftpType.position(), oftpType.length(), Command.CODEPAGE);
		field.set(this, newValue);
	}

	private final void setIntegerByBytes(Field field, final byte[] byteArray, final OFTPType oftpType)
	throws UnsupportedEncodingException, IllegalArgumentException, IllegalAccessException, NumberFormatException
	{
		String newValue;
		newValue = new String(byteArray, oftpType.position(), oftpType.length(), Command.CODEPAGE);
		Integer newInt = Integer.parseInt(newValue);
		field.set(this, newInt);
	}

	private final void setLongByBytes(Field field, final byte[] byteArray, final OFTPType oftpType)
	throws UnsupportedEncodingException, IllegalArgumentException, IllegalAccessException, NumberFormatException
	{
		String newValue;
		newValue = new String(byteArray, oftpType.position(), oftpType.length(), Command.CODEPAGE);
		Long newLong = Long.parseLong(newValue);
		field.set(this, newLong);
	}


	private final void setBigIntegerByBytes(Field field, final byte[] byteArray, final OFTPType oftpType)
	throws UnsupportedEncodingException, IllegalArgumentException, IllegalAccessException, NumberFormatException
	{
		String newValue;
		newValue = new String(byteArray, oftpType.position(), oftpType.length(), Command.CODEPAGE);
		BigInteger newBigInteger = BigInteger.valueOf(Long.parseLong(newValue));
		field.set(this, newBigInteger);
	}

	private final byte[] getBytesFromField(final Field field, final OFTPType type)
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(type.length());
		field.setAccessible(true);
		try
		{
			if (field != null)
			{
				Object fieldObject = field.get(this);
				if (fieldObject != null)
				{
					if (field.getType().isAssignableFrom(String.class))
					{
						byteBuffer.put(this.getBytesFromString((String)fieldObject, type), 0, type.length());
					}
					else if (field.getType().isAssignableFrom(Integer.class))
					{
						byteBuffer.put(this.getBytesFromInteger((Integer)fieldObject, type), 0, type.length());
					}
					else if (field.getType().isAssignableFrom(int.class))
					{
						byteBuffer.put(this.getBytesFromInteger((Integer)fieldObject, type), 0, type.length());
					}
					else if (field.getType().isAssignableFrom(Long.class))
					{
						byteBuffer.put(this.getBytesFromLong((Long)fieldObject, type), 0, type.length());
					}
					else if (field.getType().isAssignableFrom(long.class))
					{
						byteBuffer.put(this.getBytesFromLong((Long)fieldObject, type), 0, type.length());
					}
					else if (field.getType().isAssignableFrom(BigInteger.class))
					{
						byteBuffer.put(this.getBytesFromBigInteger((BigInteger)fieldObject, type), 0, type.length());
					}
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return byteBuffer.array();
	}
	
	private final byte[] getBytesFromString(String field, final OFTPType type)
	throws UnsupportedEncodingException
	{
		byte[] byteArray = null;
		String format = "%-" + String.format("%d", type.length()) + "S";
		byteArray = String.format(format, field).getBytes(Command.CODEPAGE);
		return byteArray;
	}
	
	private final byte[] getBytesFromLong(final Long field, final OFTPType type)
	throws UnsupportedEncodingException
	{
		String format = "%0" + String.format("%d", type.length()) + "d";
		return this.getBytesFromString(String.format(format, field), type);
	}

	private final byte[] getBytesFromInteger(final Integer field, final OFTPType type)
	throws UnsupportedEncodingException
	{
		String format = "%0" + String.format("%d", type.length()) + "d";
		return this.getBytesFromString(String.format(format, field), type);
	}

	private final byte[] getBytesFromBigInteger(final BigInteger field, final OFTPType type)
	throws UnsupportedEncodingException
	{
		String format = "%0" + String.format("%d", type.length()) + "d";
		return this.getBytesFromString(String.format(format, field), type);
	}

	public String toString()
	{
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append(this.getClass().getName() + "\n");
		Field[] fields = this.getClass().getDeclaredFields();
		Arrays.sort(fields, new OFTPFieldComparator());
		
		for (int ii = 0; ii < fields.length; ii++)
		{
			Field field = fields[ii];
			Annotation[] annotations = field.getDeclaredAnnotations();
			field.setAccessible(true);

			for (int jj = 0; jj < annotations.length; jj++)
			{
				Annotation annotation = annotations[jj];
				if (annotation.annotationType().isAssignableFrom(OFTPType.class))
				{
					OFTPType oftpType = (OFTPType) annotation;
					stringBuffer.append(String.format("%3d", oftpType.position()) + " | ");
					stringBuffer.append(String.format("%-12S", field.getName()) + " | ");
					stringBuffer.append(oftpType.format() + " ");
					stringBuffer.append(oftpType.type() + "(");
					stringBuffer.append(String.format("%3d", oftpType.length()));
					stringBuffer.append(") | ");
					try {
						stringBuffer.append(field.get(this));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					stringBuffer.append("\n");
				}
			}
		}
		return stringBuffer.toString();
	}
	
	final class OFTPFieldComparator
	implements Comparator<Field>
	{
		@Override
		public int compare(Field arg0, Field arg1)
		{
			int rc = 0;
			OFTPType type0 = null;
			OFTPType type1 = null;
			
			Annotation[] annotations0 = arg0.getDeclaredAnnotations();
			Annotation[] annotations1 = arg1.getDeclaredAnnotations();

			if (annotations0 != null)
			{
				for (int jj = 0; jj < annotations0.length; jj++)
				{
					Annotation annotation = annotations0[jj];
					if (annotation.annotationType().isAssignableFrom(OFTPType.class))
					{
						type0 = (OFTPType) annotation;
					}
				}
			}
			
			if (annotations1 != null)
			{
				for (int jj = 0; jj < annotations1.length; jj++)
				{
					Annotation annotation = annotations1[jj];
					if (annotation.annotationType().isAssignableFrom(OFTPType.class))
					{
						type1 = (OFTPType) annotation;
					}
				}
			}
			
			if ((type0 != null) && (type1 != null))
			{
				if (type0.position() < type1.position())
				{
					rc = -1;
				}
				else if (type0.position() > type1.position())
				{
					rc = 1;
				}
				else
				{
					rc = 0;
				}
			}
			else
			{
				rc = 0;
			}
			return rc;
		}
	}
}

