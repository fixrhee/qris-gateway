/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jpos.iso.ISOUtil;

/**
 * 
 * @author Fikri Ilyas
 */
public abstract class HelperTools {

	private static Logger logger = Logger.getLogger(HelperTools.class);

	public static String GetCurrentDate() {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return format.format(date);
	}

	public static String GetDate(String form) {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(form);
		return format.format(date);
	}
	
	public static String generateTraceNum(){
		  
		  Random rnd = new Random();
		  int n = 10000000 + rnd.nextInt(90000000);
		  String a = Integer.toString(n);
		  return a;
	  }
	
	public static String isoReader(byte[] message) {
		String decrypt = ISOUtil.hexString(message);
		String result = new String(ISOUtil.hex2byte(decrypt));
		return result;
	}

	public static String getFutureDate(int periode, String format) {
		GregorianCalendar calendar = new GregorianCalendar();
		Date now = calendar.getTime();
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		String formattedDate = fmt.format(now);
		calendar.add(GregorianCalendar.DAY_OF_MONTH, periode);
		Date tomorrow = calendar.getTime();
		formattedDate = fmt.format(tomorrow);
		return formattedDate;
	}

	public static String xmlDate() throws DatatypeConfigurationException {
		GregorianCalendar gc = new GregorianCalendar();
		DatatypeFactory dtf = DatatypeFactory.newInstance();
		XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);
		return xgc.toXMLFormat();
	}

	public static XMLGregorianCalendar calXmlDate()
			throws DatatypeConfigurationException {
		GregorianCalendar gc = new GregorianCalendar();
		DatatypeFactory dtf = DatatypeFactory.newInstance();
		XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);
		return xgc;
	}

	public static XMLGregorianCalendar stringToXMLGregorianCalendar(String s)
			throws ParseException, DatatypeConfigurationException {
		XMLGregorianCalendar result = null;
		Date date;
		SimpleDateFormat simpleDateFormat;
		GregorianCalendar gregorianCalendar;

		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		date = simpleDateFormat.parse(s);
		gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
		gregorianCalendar.setTime(date);
		result = DatatypeFactory.newInstance().newXMLGregorianCalendar(
				gregorianCalendar);
		return result;
	}

	public static String generateUID(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = length; i > 0; i -= 12) {
			int n = Math.min(12, Math.abs(i));
			sb.append(StringUtils.leftPad(Long.toString(
					Math.round(Math.random() * Math.pow(36D, n)), 36), n, '0'));
		}

		return sb.toString();
	}

	public static HashMap<String, Object> mergeFields(ArrayList header,
			ArrayList value) {
		HashMap m = new HashMap<String, Object>();
		for (int i = 0; i < header.size(); i++) {
			m.put(header.get(i), value.get(i));
		}
		return m;
	}

	public static boolean validateArray(String[] source, String keyword) {
		ArrayList al = new ArrayList<String>(Arrays.asList(source));
		return al.contains(keyword);
	}

	public static Integer validateArrayPosition(String[] array, String keyword) {
		Integer count = -1;

		for (int i = 0; i < array.length; i++) {
			if (array[i].contains(keyword)) {
				count = i;
			}
		}

		return count;
	}

	/**
	 * created by edw
	 * 
	 * @param dateFormat
	 * @return formatted date
	 */
	public static String getFormattedDate(String dateFormat) {
		return new SimpleDateFormat(dateFormat).format(new Date());
	}

	/**
	 * created by edw
	 * 
	 * @param name
	 * @param description
	 * @param username
	 * @param amount
	 * @param status
	 * @param transactionNumber
	 * @param transferType
	 * @param transactionDate
	 * @param parentTransactionNumber
	 * @return hashmaps[]
	 */
	@SuppressWarnings("unchecked")
	public static HashMap[] constructAccountHistoryDetails(List<String> name,
			List<String> description, List<String> username,
			List<String> amount, List<String> status,
			List<String> transactionNumber, List<String> transferType,
			List<String> transactionDate, List<String> parentTransactionNumber) {
		HashMap[] maps = new HashMap[name.size()];

		for (int i = 0; i < name.size(); i++) {
			HashMap hashMap = new HashMap<>();

			hashMap.put("name", name.get(i));
			hashMap.put("description", description.get(i));
			hashMap.put("username", username.get(i));
			hashMap.put("amount", amount.get(i));
			hashMap.put("status", status.get(i));
			hashMap.put("transactionNumber", transactionNumber.get(i));
			hashMap.put("transferType", transferType.get(i));
			hashMap.put("transactionDate", transactionDate.get(i));
			hashMap.put("parentTransactionNumber",
					parentTransactionNumber.get(i));

			maps[i] = hashMap;
		}

		return maps;
	}

	/**
	 * created by edw
	 * 
	 * @param headers
	 * @param values
	 * @param internalName
	 * @return string value from selected header
	 */
	public static String getField(List<String> headers, List<String> values,
			String internalName) {

		if (headers == null) {
			logger.info("header is null");
			return "";
		}

		if (values == null) {
			logger.info("value is null");
			return "";
		}

		if (!headers.contains(internalName)) {
			logger.info("header does not contain " + internalName);
			return "";
		}

		int i = 0;
		for (String header : headers) {
			if (header.equals(internalName))
				break;
			i++;
		}
		return (values.get(i) != null) ? values.get(i) : "";
	}
	// -------------------- Mandiri utils checker

				public static String cekAmount(String number)
	/*     */   {
	/* 185 */      if (number!=null) {
		/*  56 */         char[] arrays = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
		/*  57 */         //StringUtils utils = new StringUtils();
						  if (number.trim().length() == 0) {
							  logger.info("[jumlah harus di isi]");
							  return "AMOUNT_MUST_BE_FILLED";
		}
		/*  58 */         else if (StringUtils.containsOnly(number, arrays)) {
							return number;
		/*     */         }
						  else{
		/*  67 */         logger.info("[Jumlah hanya boleh di isi dengan numeric saja.]");
		/*  68 */         return "AMOUNT_IS_NOT_NUMERIC_ONLY";
						  }
		/*     */       }
					return "AMOUNT_MUST_BE_FILLED";
	/*     */   }
				
				public static String cekPin(String pin)
				{
					if (pin.length()==6) {
						char[] arrays = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
						//StringUtils utils = new StringUtils();
						if (pin.trim().length() == 0) {
							  logger.info("[PIN harus di isi]");
							  return "PIN_MUST_BE_FILLED";
						}
						else if (StringUtils.containsOnly(pin, arrays)) {
											return pin;
						}
						else {
							logger.info("[No PIN hanya boleh di isi dengan numeric saja.]");
							return "PIN_IS_NOT_NUMERIC_ONLY";
						}
					}
					return "PIN_LENGTH_IS_NOT_SIX_CHAR";
				}
				
				public static String cekNorek(String norek)
				{
					if (norek!=null) {
						
						char[] arrays = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
						//StringUtils utils = new StringUtils();
						if (norek.trim().length() == 0) {
						       logger.info("[rekening harus di isi]");
						       return "REKENING_MUST_BE_FILLED";
						}
						else if (StringUtils.containsOnly(norek, arrays)) {
											return norek;
						}
						else{
						logger.info("[No Rekening hanya boleh di isi dengan numeric saja.]");
						return "IS_NOT_NUMERIC_ONLY";
						}
					}
					return "REKENING_MUST_BE_FILLED";
				}
				public static String cekKeterangan(String keterangan){
				
				if (keterangan.trim().length() == 0) {
					logger.info("[tidak terdapat keterangan]");
					return "Tidak Ada Deskripsi";
				}
					else  {
						return keterangan;
				}
					
				}
				

				// -------------------- Mandiri utils checker end

}
