package Model;

import javafx.util.Pair;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

enum types
{
	UNDEFINED,
	NUMBER_SMALLER_THAN_1K, // 0<=x<1K
	NUMBER_1K_TO_1M, // 1K<=x<1M
	NUMBER_1M_TO_1B, // 1M<=x<1B
	NUMBER_GREATER_THAN_1B, //x>=1B
	NUMBER_ENDS_WITH_$,
	NUMBER_ENDS_WITH_SIGN_PERCENTAGE,// 123%
	NUMBER_ENDS_WITH_SIGN_$, //23$
	NUMBER_FRACTION // 1/2
}

public class Parser implements IParser{
	//pre-parse
	Pattern patternSpaces = Pattern.compile("[ ]{1,}");

	//Percentage
	Pattern patternEndsWithPercentageSign = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ]{1,})?[%]"); //Number%
	Pattern patternEndsWithPercent = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?[ ]{1,}(percent)");//Number percent
	Pattern patternEndsWithPercentage = Pattern.compile("[0-9]{1,}([\\\\.][0-9]{1,})?[ ]{1,}(percentage)");//Number percentage

	//Date
	Pattern patternDDMonth = Pattern.compile("[0-3]{1}[0-9]{1}[ ](?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)");//DD Month
	Pattern patternMonthDD = Pattern.compile("(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)[ ][0-3]{1}[0-9]{1}\\D");//Month DD
	Pattern patternMonthYear = Pattern.compile("(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)[ ][1-2]{1}[0-9]{1}[0-9]{1}[0-9]{1}");//Month year

	//Price
	Pattern patternDollarSignBillion = Pattern.compile("[$](\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](billion)|[$]\\d+\\.?\\d*[ ](billion)");//$price billion
	Pattern patternDollarSignMillion = Pattern.compile("[$](\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](million)|[$]\\d+\\.?\\d*[ ](million)");//$price million
	Pattern patternDollarSign = Pattern.compile("[$](\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?|[$]\\d+\\.?\\d*");//$price
	Pattern patternFractionDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](\\d{1,}+\\/+\\d{1,})[ ](Dollars)|\\d+\\.?\\d*[ ](\\d{1,}+\\/+\\d{1,})[ ](Dollars)");//Price Dollars
	Pattern patternDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](Dollars)|\\d+\\.?\\d*[ ](Dollars)"); //Price Dollars
	Pattern patternMDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ]?(m Dollars)|\\d+\\.?\\d*[ ]?(m Dollars)");//Price m Dollars
	Pattern patternBnDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ]?(bn Dollars)|\\d+\\.?\\d*[ ]?(bn Dollars)");//Price bn Dollars
	Pattern patternBillionUSDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](billion U.S. dollars)|\\d+\\.?\\d*[ ](billion U.S. dollars)");//Price billion U.S. dollars
	Pattern patternMillionUSDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](million U.S. dollars)|\\d+\\.?\\d*[ ](million U.S. dollars)");//Price million U.S. dollars
	Pattern patternTrillionUSDollar = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?[ ](trillion U.S. dollars)|\\d+\\.?\\d*[ ](trillion U.S. dollars)");//Price trillion U.S. dollars

	//expressions
	Pattern patternWordWordWord= Pattern.compile("[A-z]{1,}('|'[A-z]{1,})?[-][A-z]{1,}('|'[A-z]{1,})?[-][A-z]{1,}('|'[A-z]{1,})?"); //Word-word-word (for example: step-by-step)
	Pattern patternWordWord = Pattern.compile("[A-z]{1,}('|'[A-z]{1,})?[-][A-z]{1,}('|'[A-z]{1,})?"); //Word-word (for example: Value-added)
	Pattern patternNumberWord= Pattern.compile("\\d{1,}[-][A-z]{1,}('|'[A-z]{1,})?");  //Number-word (for example: 10-part )
	Pattern patternWordNumber= Pattern.compile("[A-z]{1,}('|'[A-z]{1,})?[-]\\d{1,}"); //Word-Number (for example: part-10 )
	Pattern patternNumberNumber= Pattern.compile("\\d{1,}[-]\\d{1,}");//Number-number (for example: 6-7)
	Pattern patternBetweenNumberAndNumber= Pattern.compile("(Between |between )\\d{1,}( and )\\d{1,}");//Between number and number (for example: between 18 and 24)		 

	//numbers
	Pattern patternNumbers = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?");
	Pattern patternNumbersFraction = Pattern.compile("([0-9]{1,3})?[ ](\\d{1,}+\\/+\\d{1,})");
	Pattern patternNumbersCommas = Pattern.compile("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?|[$]\\d+\\.?\\d*");
	Pattern patternNumbersThousand = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ]{1,})?[ ](Thousand)");
	Pattern patternNumbersMillion = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ]{1,})?[ ](Million)");
	Pattern patternNumbersBillion = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ]{1,})?[ ](Billion)");

	
	//first string is the term string,int is the count
	public HashMap<String,Integer> parseDoc(String doc_Text, String doc_Number)
	{
		HashMap<String,Integer> termsHash= new HashMap<>();
		StringBuffer sb1 = new StringBuffer(),sb2=new StringBuffer() ;

		sb1=parseSpaces(termsHash, doc_Text);
		sb2=parsePercent(termsHash, sb1);
		sb1=parseDate(termsHash,sb2);
		sb2=parsePrices(termsHash,sb1);
		sb1=parseExpressions(termsHash,sb2);
		sb2=parseNumbers(termsHash,sb1);

		parseEndWords(termsHash,sb2);

		//printHash(termsHash);
	    
		return termsHash;
	}
	
	//TODO-in case of number keep them too
	void parseEndWords(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer(),sb2=new StringBuffer() ;
		Matcher matcher  = Pattern.compile("\\(|\\)|\\]|\\[|\\\\|\\/|,|:|\\$|\\.|\\+|\\*|-").matcher(doc_Text);
		while (matcher.find())
		{
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);
		
		String[] allWords = sb1.toString().split(" ");		
	    int allWordsLength = allWords.length;
	    System.out.println("leftover size: "+allWords.length);
	    for(int i = 0; i<allWordsLength; i++)
        {
	    	
	    	if(allWords[i].matches("[A-z]{2,}('|'[A-z]{1,})?[A-z]{1,}"))
	    	{
	    		System.out.println("adding leftovers: "+allWords[i]);
	    		addToHash(terms_Hash,allWords[i]);
	    	}
	    	else
	    	{
	    		System.out.println("not adding: "+allWords[i]);
	    	}
        }
	    
	}

	void printHash(HashMap<String,Integer> terms_Hash)
	{
		for (String term: terms_Hash.keySet()){
			String key = term.toString();
			System.out.println("term is: "+key+" value is :"+terms_Hash.get(key));
		}
	}


	StringBuffer parseSpaces(HashMap<String,Integer> terms_Hash, String doc_Text )
	{
		StringBuffer sb1 = new StringBuffer();

		Matcher matcher  = patternSpaces.matcher(doc_Text);
		while (matcher.find())
		{
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		return sb1;
	}

	StringBuffer parseExpressions(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		 
		Matcher matcher=patternWordWordWord.matcher(doc_Text);
		System.out.println("start patternWordWordWord !!!");
		while (matcher.find()) 
		{
			System.out.println("match: "+matcher.group());
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher=patternWordWord.matcher(sb1);
		System.out.println("start patternWordWord !!!");
		while (matcher.find()) 
		{
			System.out.println("match: "+matcher.group());
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher=patternNumberWord.matcher(sb2);
		System.out.println("start patternNumberWord !!!");
		while (matcher.find()) 
		{
			System.out.println("match: "+matcher.group());
			addpatternNumberWordToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);
		
		sb2.setLength(0);
		matcher=patternWordNumber.matcher(sb1);
		System.out.println("start patternWordNumber !!!");
		while (matcher.find()) 
		{
			System.out.println("match: "+matcher.group());
			addpatternWordNumberToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);
		
		sb1.setLength(0);
		matcher=patternNumberNumber.matcher(sb2);
		System.out.println("start patternNumberNumber !!!");
		while (matcher.find()) 
		{
			System.out.println("match: "+matcher.group());
			addpatternNumberNumberToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);
			
		sb2.setLength(0);
		matcher=patternBetweenNumberAndNumber.matcher(sb1);
		System.out.println("start patternBetweenNumberAndNumber !!!");
		while (matcher.find()) 
		{
			System.out.println("match: "+matcher.group());
			addToHash(terms_Hash,matcher.group());//not clearing- we want to add as regular number too
		}
		matcher.appendTail(sb2);
			 
	
		return sb2;
	}


	private void addpatternWordNumberToHash(HashMap<String, Integer> terms_Hash, String match)
	{
		int idx=match.indexOf("-");
		
		addToHash(terms_Hash,match);	
		if( idx!=(-1) )
		{
			addToHash(terms_Hash,match.substring(idx+1,match.length()).toString());
		}
	}

	private void addpatternNumberNumberToHash(HashMap<String, Integer> terms_Hash, String match)
	{
		int idx=match.indexOf("-");
		
		addToHash(terms_Hash,match);
		if( idx!=(-1) )
		{
			addToHash(terms_Hash,match.substring(0,idx).toString());
			addToHash(terms_Hash,match.substring(idx+1,match.length()).toString());
		}
	}

	private void addpatternNumberWordToHash(HashMap<String, Integer> terms_Hash, String match) 
	{
		int idx=match.indexOf("-");
		
		addToHash(terms_Hash,match);
		if( idx!=(-1) )
		{
			addToHash(terms_Hash,match.substring(0,idx).toString());
		}
	}

	StringBuffer parseNumbers(HashMap<String,Integer>terms_Hash, StringBuffer doc_Text)
	{

		System.out.println("start parseNumbers !!!");
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher=patternNumbersThousand.matcher(doc_Text);
		System.out.println("start patternNumbersThousand !!!");
		while (matcher.find())
		{
			addpatternNumbersThousandToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher=patternNumbersMillion.matcher(sb1);
		System.out.println("start patternNumbersMillion !!!");
		while (matcher.find())
		{
			addpatternNumbersMillionToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher=patternNumbersBillion.matcher(sb2);
		System.out.println("start patternNumbersBillion !!!");
		while (matcher.find())
		{
			addpatternNumbersBillionToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		System.out.println("start patternNumbersFraction !!!");
		matcher=patternNumbersFraction.matcher(sb1);
		sb2.setLength(0);
		while (matcher.find())
		{
			addToHash(terms_Hash,matcher.group());
		}
		matcher.appendTail(sb2);


		System.out.println("start patternNumbersCommas !!!");
		matcher=patternNumbersCommas.matcher(sb2);
		sb1.setLength(0);
		while (matcher.find())
		{
			addpatternNumbersCommasToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher=patternNumbers.matcher(sb1);
		sb2.setLength(0);
		System.out.println("start patternNumbers !!!");
		while (matcher.find())
		{
			addpatternNumbersToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);


		return sb2;
	}

	void addpatternNumbersThousandToHash(HashMap<String,Integer> termsHash,String match )
	{
		System.out.println("match Thousand: "+ match);
		addToHash(termsHash,match.subSequence(1,match.length()-"Thousand".length()-1).toString()+"K");
	}
	void addpatternNumbersMillionToHash(HashMap<String,Integer> termsHash,String match )
	{
		System.out.println("match Million: "+ match);
		addToHash(termsHash,match.subSequence(1,match.length()-"Million".length()-1).toString()+"M");
	}
	void addpatternNumbersBillionToHash(HashMap<String,Integer> termsHash, String match )
	{
		System.out.println("match Billion: "+ match);
		addToHash(termsHash,match.subSequence(1,match.length()-"Billion".length()-1).toString()+"B");
	}

	void addpatternNumbersToHash(HashMap<String,Integer> termsHash,String match )
	{
		System.out.println("match addpatternNumbersToHash: "+ match);
		match= upToThreeNubersAfterDot(match);
		types t = checkNumSize(match);
		Float num;
		if( t == types.NUMBER_SMALLER_THAN_1K)
		{
			addToHash(termsHash,match);
		}
		else if(t == types.NUMBER_1K_TO_1M )
		{
			num=Float.parseFloat(match);
			num=num/1000;
			addToHash(termsHash,num.toString()+"K");
		}
		else if(t == types.NUMBER_1M_TO_1B)
		{
			num=Float.parseFloat(match);
			num=num/1000000;
			addToHash(termsHash,String.format("%.3f", num)+"M");
		}
		else if(t == types.NUMBER_GREATER_THAN_1B)
		{
			num=Float.parseFloat(match);
			num=num/1000000000;
			addToHash(termsHash,String.format("%.3f", num)+"B");
		}
	}

	void addpatternNumbersCommasToHash(HashMap<String,Integer> termsHash,String match )
	{
		System.out.println("match Commas: "+ match);
		match=removeComma(match);
		addpatternNumbersToHash(termsHash,match);
	}

	StringBuffer parsePrices(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{

		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher  = patternDollarSignMillion.matcher(doc_Text);//$price million
		while (matcher.find())
		{
			System.out.println("match patternDollarSignMillion: "+ matcher.group());
			addpatternDollarSignMillionToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher  = patternDollarSignBillion.matcher(sb1);//$price billion
		while (matcher.find())
		{
			System.out.println("match patternDollarSignBillion: "+ matcher.group());
			addpatternDollarSignBillionToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);
		sb1.setLength(0);

		matcher  = patternDollarSign.matcher(sb2);//$price
		while (matcher.find())
		{
			System.out.println("match patternDollarSign: "+ matcher.group());
			addpatternDollarSignToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternFractionDollar.matcher(sb1);//Price fraction Dollars
		while (matcher.find())
		{
			System.out.println("match patternFractionDollar: "+ matcher.group());
			addpatternFractionDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternDollar.matcher(sb2);//Price Dollars
		while (matcher.find())
		{
			System.out.println("match patternDollar: "+ matcher.group());
			addpatternDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternMDollar.matcher(sb1);//Price m Dollars
		while (matcher.find())
		{
			System.out.println("match patternMDollar: "+ matcher.group());
			addpatternMDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternBnDollar.matcher(sb2);//Price bn Dollars
		while (matcher.find())
		{
			System.out.println("match patternBnDollar: "+ matcher.group());
			addpatternBnDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternBillionUSDollar.matcher(sb1);//Price billion U.S. dollars
		while (matcher.find())
		{
			System.out.println("match patternBillionUSDollar: "+ matcher.group());
			addpatternBillionUSDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternMillionUSDollar.matcher(sb2);//Price million U.S. dollars
		while (matcher.find())
		{
			System.out.println("match patternMillionUSDollar: "+ matcher.group());
			addpatternMillionUSDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternTrillionUSDollar.matcher(sb1);//Price trillion U.S. dollars
		while (matcher.find())
		{
			System.out.println("match patternTrillionUSDollar: "+ matcher.group());
			addpatternTrillionUSDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		return sb2;
	}

	void addpatternDollarSignMillionToHash(HashMap<String,Integer> termsHash,String match )//$price million -> price M Dollars
	{
		System.out.println("match: "+ match);
		addToHash(termsHash,match.subSequence(1,match.length()-"million".length()-1).toString()+" M Dollars");
	}

	void addpatternDollarSignBillionToHash(HashMap<String,Integer> termsHash,String match )//$price billion ->price M Dollars
	{
		System.out.println("match: "+ match);
		addToHash(termsHash,match.subSequence(1,match.length()-"billion".length()-1).toString()+"000"+" M Dollars");;
	}

	void addpatternDollarSignToHash(HashMap<String,Integer> termsHash,String match )//$price -> above 1M- save as price M Dollars, else save as price Dollars
	{
		System.out.println("match: "+ match);
		match= upToThreeNubersAfterDot(match.subSequence(1,match.length()).toString());
		match=removeComma(match);
		types t = checkNumSize(match);
		if( t == types.NUMBER_GREATER_THAN_1B || t == types.NUMBER_1M_TO_1B)
		{
			addToHash(termsHash,match.substring(0,match.length()-6)+" M Dollars");
		}
		else if(t == types.NUMBER_1K_TO_1M || t == types.NUMBER_SMALLER_THAN_1K)
		{
			addToHash(termsHash,match.substring(0,match.length())+" Dollars");
		}
	}

	void addpatternFractionDollarToHash(HashMap<String,Integer> termsHash,String match )//Price fraction Dollars-> save the same
	{
		System.out.println("match: "+ match);
		addToHash(termsHash,match);
	}

	void addpatternDollarToHash(HashMap<String,Integer> termsHash,String match )//Price Dollars-> above 1M- save as price M Dollars, else save as price Dollars
	{
		System.out.println("match: "+ match);
		match= upToThreeNubersAfterDot(match.subSequence(0,match.length()-"Dollars".length()-1).toString());
		match=removeComma(match);
		types t = checkNumSize(match);
		if( t == types.NUMBER_GREATER_THAN_1B || t == types.NUMBER_1M_TO_1B)
		{
			addToHash(termsHash,match.substring(0,match.length()-6)+" M Dollars");
		}
		else if(t == types.NUMBER_1K_TO_1M || t == types.NUMBER_SMALLER_THAN_1K)
		{
			addToHash(termsHash,match.substring(0,match.length())+" Dollars");
		}
	}

	void addpatternMDollarToHash(HashMap<String,Integer> termsHash,String match )//Price m Dollars --> Price M Dollars
	{
		System.out.println("match: "+ match);
		int idxspace=match.indexOf(" ");
		int idxm=match.indexOf("m");
		int min;

		if( idxm!=(-1) && idxspace!=(-1) )
		{
			min= idxm<idxspace? idxm:idxspace;
			addToHash(termsHash,match.substring(0,min)+" M Dollars");
		}
		else if (idxm!=(-1))
		{
			addToHash(termsHash,match.substring(0,idxm)+" M Dollars");
		}
	}

	void addpatternBnDollarToHash(HashMap<String,Integer> termsHash,String match )//Price bn Dollars
	{
		System.out.println("match: "+ match);
		match=removeComma(match);
		int idxspace=match.indexOf(" ");
		int idxb=match.indexOf("b");
		int min;
		if( idxb!=(-1) && idxspace!=(-1) )
		{
			min= idxb<idxspace? idxb:idxspace;
			addToHash(termsHash,match.substring(0,min)+"000 M Dollars");
		}
		else if (idxb!=(-1))
		{
			addToHash(termsHash,match.substring(0,idxb)+"000 M Dollars");
		}
	}

	void addpatternBillionUSDollarToHash(HashMap<String,Integer> termsHash,String match )//Price billion U.S. dollars
	{
		System.out.println("match: "+ match);
		match=removeComma(match);
		int idxspace=match.indexOf(" ");

		addToHash(termsHash,match.substring(0,idxspace)+"000 M Dollars");
	}

	void addpatternMillionUSDollarToHash(HashMap<String,Integer>termsHash, String match )//Price million U.S. dollars
	{
		System.out.println("match: "+ match);
		match=removeComma(match);
		int idxspace=match.indexOf(" ");

		addToHash(termsHash,match.substring(0,idxspace)+" M Dollars");
	}

	void addpatternTrillionUSDollarToHash(HashMap<String,Integer> termsHash, String match )//Price trillion U.S. dollars
	{
		System.out.println("match: "+ match);
		match=removeComma(match);
		int idxspace=match.indexOf(" ");

		addToHash(termsHash,match.substring(0,idxspace)+"000000 M Dollars");
	}

	String removeComma(String num)
	{
		StringBuffer sb1 = new StringBuffer();

		Matcher matcher  = Pattern.compile("[,]").matcher(num);
		while (matcher.find())
		{
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);

		return sb1.toString();
	}

	String upToThreeNubersAfterDot(String num)
	{
		//TODO -if the word is a number and he got more then 3 numbers after the dot (.)
		if(Pattern.matches("[0-9]{1,}[.][0-9]{4,}",num))
		{
			int idx=num.indexOf(".");
			return num.subSequence(0, idx+3).toString();
		}

		return num.toString();
	}

	StringBuffer parsePercent(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher  = patternEndsWithPercentageSign.matcher(doc_Text);
		while (matcher.find())
		{
			addToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher  = patternEndsWithPercent.matcher(sb1);
		while (matcher.find())
		{
			addToHash(terms_Hash, sb1.substring(matcher.start(),matcher.end()-8)+"%");
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		matcher  = patternEndsWithPercentage.matcher(sb2);
		sb1.setLength(0);
		while (matcher.find())
		{
			addToHash(terms_Hash, sb2.substring(matcher.start(),matcher.end()-11)+"%");
			matcher.appendReplacement(sb1, " ");
		}

		matcher.appendTail(sb1);

		return sb1;
	}

	StringBuffer parseDate(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher  = patternDDMonth.matcher(doc_Text);
		while (matcher.find())
		{
			//System.out.println("match: "+ matcher.group());
			addpatternDDMonthToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher  = patternMonthDD.matcher(sb1);
		while (matcher.find())
		{
			//System.out.println("match: "+ matcher.group());
			addpatternMonthDDToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternMonthYear.matcher(sb2);
		while (matcher.find())
		{
			//System.out.println("match year: "+ matcher.group());
			addpatternMonthYearToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		return sb1;
	}

	void addpatternDDMonthToHash(HashMap<String,Integer>termsHash,String match )
	{
		String month= MonthToNum(match.subSequence(3, match.length()).toString());
		addToHash(termsHash,month+"-"+match.subSequence(0,2).toString());
	}

	void addpatternMonthDDToHash(HashMap<String,Integer> termsHash,String match )
	{
		String month= MonthToNum(match.subSequence(0, match.length()-4).toString());
		addToHash(termsHash,month+"-"+match.subSequence(match.length()-3,match.length()-1).toString());
	}

	void addpatternMonthYearToHash(HashMap<String,Integer> termsHash,String match )
	{
		String month= MonthToNum(match.subSequence(0, match.length()-5).toString());
		addToHash(termsHash,match.subSequence(match.length()-4,match.length()).toString()+"-"+month);
	}

	String MonthToNum(String month)
	{
		switch (month)
		{
			case "Jan":
			case "January":
				return "01";
			case "Feb":
			case "February":
				return "02";
			case "Mar":
			case "March":
				return "03";
			case "Apr":
			case "April":
				return "04";
			case "May":
				return "05";
			case "Jun":
			case "Jane":
				return "06";
			case "Jul":
			case "July":
				return "07";
			case "Aug":
			case "August":
				return "08";
			case "Sep":
			case "September":
				return "09";
			case "Oct":
			case "October":
				return "10";
			case "Nov":
			case "November":
				return "11";
			case "Dec":
			case "December":
				return "12";
		}

		return "00";
	}

	void addToHash(HashMap<String,Integer> termsHash, String term )
	{
		System.out.println("adding: "+ term);
		if(!termsHash.containsKey(term))
		{
			termsHash.put(term,1);
		}
		else
		{
			termsHash.put(term,termsHash.get(term)+1);
		}
	}

	String buildPrice(String number)
	{
		String numberRet=number;
		types e= checkNumSize(number);

		switch (e){
			case NUMBER_SMALLER_THAN_1K:
			case NUMBER_1K_TO_1M:
				numberRet=number+" Dollars";
				break;
			case NUMBER_1M_TO_1B:
			case NUMBER_GREATER_THAN_1B:
				numberRet=number+" M Dollars";
				break;
		}
		return numberRet;
	}

	private types checkNumSize(String number)
	{
		types ret=types.UNDEFINED;
		float num;

		try
		{
			num=Float.parseFloat(number);

			if(num<1000)
			{
				ret=types.NUMBER_SMALLER_THAN_1K;
			}
			else if(num<1000000)
			{
				ret=types.NUMBER_1K_TO_1M;
			}
			else if(num<1000000000)
			{
				ret=types.NUMBER_1M_TO_1B;
			}
			else
			{
				ret=types.NUMBER_GREATER_THAN_1B;
			}
		}
		catch (NumberFormatException e)
		{
			System.out.println("ERROR:checkNumSize - " + number);
		}

		return ret;
	}
}
