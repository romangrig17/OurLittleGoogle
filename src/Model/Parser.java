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
	
	//Names
	Pattern patternEntity= Pattern.compile("[A-Z]{1}[a-z]{1,}[ ][A-Z]{1,}[a-z]{1,}([ |-][A-Z]{1}[a-z]{1,}([ |-][A-Z]{1,}[a-z]{1,})?)?");

	//KG
	Pattern patternKG= Pattern.compile("[0-9]{1,}([\\\\.][0-9]{1,})?([ ])?(KG|kg|Kg|kilogram)");
	
	//phone number
	Pattern patternPhoneNumber= Pattern.compile("[0-9]{3}(-| )[0-9]{3}( )?[0-9]{4}");
	Pattern patternPhoneNumber2= Pattern.compile("\\([0-9]{3}\\)[ ]?[0-9]{3}[ ]?[0-9]{4}");	
	

	//This function parse the input doc_text.
	//The output is a hashmap of all the parsed words:
	//first string is the term,int is the count.
	public HashMap<String,Integer> parseDoc(String doc_Text, String doc_Number)
	{
		HashMap<String,Integer> termsHash= new HashMap<>();
		StringBuffer sb1 = new StringBuffer(doc_Text),sb2=new StringBuffer() ;

		parseNames(termsHash,sb1);
		sb2=parsePercent(termsHash, sb1);
		sb1=parseDate(termsHash,sb2);
		sb2=parsePrices(termsHash,sb1);
		sb1=parsePhoneNumber(termsHash,sb2);
		sb2=parseExpressions(termsHash,sb1);
		sb1=parseKG(termsHash,sb2);				
		sb2=parseNumbers(termsHash,sb1);

		parseEndWords(termsHash,sb2);

		//printHash(termsHash); for checking you can print
	    
		return termsHash;
	}
	
	//parsePhoneNumber- input: stringBuffer of the text and hashMap to update
	//output:the new string buffer without the matches+ updates the the hash with the matched numbers
	StringBuffer parsePhoneNumber(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer() ;
		StringBuffer sb2 = new StringBuffer();
		
		Matcher matcher  = patternPhoneNumber.matcher(doc_Text);
		while (matcher.find())
		{
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);
		
		matcher  = patternPhoneNumber2.matcher(sb1);
		while (matcher.find())
		{
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, "");
		}
		matcher.appendTail(sb2);
		
		return sb2;
	}

	//parseKG- input: stringBuffer of the text and hashMap to update
	//output:the new string buffer without the matches+ updates the the hash with the matched kilograms
	StringBuffer parseKG(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer() ;
		Matcher matcher  = patternKG.matcher(doc_Text);
		while (matcher.find())
		{
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);
		return sb1;
	}
	
	//parseNames- input: stringBuffer of the text and hashMap to update
	//no output, just updates the the hash with the matched names and entities 
	void parseNames(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer() ;
		Matcher matcher  = patternEntity.matcher(doc_Text);
		while (matcher.find())
		{
			addToHash(terms_Hash,"!"+matcher.group());
		}
		matcher.appendTail(sb1);
	}

	//parseEndWords- input: stringBuffer of the text and hashMap to update
	//no output, just updates the the hash with the matched names and entities 
	void parseEndWords(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer() ;
		Matcher matcher  = Pattern.compile("\\(|\\)|\\]|\\[|\\\\|\\/|,|:|\\$|\\.|\\+|\\*|-|'|'|_|`|!|@|#|%|\\^|\"|&|;|\\?").matcher(doc_Text);
		while (matcher.find())
		{
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);
		
		String[] allWords = sb1.toString().split(" ");		
	    int allWordsLength = allWords.length;
	    for(int i = 0; i<allWordsLength; i++)
        {
	    	
	    	if(allWords[i].matches("[A-z]{2,}"))
	    	{
	    		addToHash(terms_Hash,allWords[i]);
	    	}
        }
	}

	//printHash- input: hashMap
	//printout the hash values
	void printHash(HashMap<String,Integer> terms_Hash)
	{
		for (String term: terms_Hash.keySet()){
			String key = term.toString();
			System.out.println("term is: "+key+" value is :"+terms_Hash.get(key));
		}
	}

	//parseExpressions- input: stringBuffer of the text and hashMap to update
	//output: the new text without the matches+updates the the hash with the matched expressions 
	StringBuffer parseExpressions(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		 
		Matcher matcher=patternWordWordWord.matcher(doc_Text);
		while (matcher.find()) 
		{
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher=patternWordWord.matcher(sb1);
		while (matcher.find()) 
		{
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher=patternNumberWord.matcher(sb2);;
		while (matcher.find()) 
		{
			addpatternNumberWordToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);
		
		sb2.setLength(0);
		matcher=patternWordNumber.matcher(sb1);
		while (matcher.find()) 
		{
			addpatternWordNumberToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);
		
		sb1.setLength(0);
		matcher=patternNumberNumber.matcher(sb2);
		while (matcher.find()) 
		{
			addpatternNumberNumberToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);
			
		sb2.setLength(0);
		matcher=patternBetweenNumberAndNumber.matcher(sb1);;
		while (matcher.find()) 
		{
			addToHash(terms_Hash,matcher.group());//not clearing- we want to add as regular number too
		}
		matcher.appendTail(sb2);
			 
	
		return sb2;
	}

	//addpatternWordNumberToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	private void addpatternWordNumberToHash(HashMap<String, Integer> terms_Hash, String match)
	{
		int idx=match.indexOf("-");
		
		addToHash(terms_Hash,match);	
		if( idx!=(-1) )
		{
			addToHash(terms_Hash,match.substring(0,idx-1).toString());
			addToHash(terms_Hash,match.substring(idx+1,match.length()).toString());
		}
	}

	//addpatternNumberNumberToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
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
	
	//addpatternNumberWordToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	private void addpatternNumberWordToHash(HashMap<String, Integer> terms_Hash, String match) 
	{
		int idx=match.indexOf("-");
		
		addToHash(terms_Hash,match);
		if( idx!=(-1) )
		{
			addToHash(terms_Hash,match.substring(0,idx).toString());
		}
	}

	//parseNumbers: input- hash and doc_text
	//output: add the matched numbers to the hash table and returns the new text without the matches
	StringBuffer parseNumbers(HashMap<String,Integer>terms_Hash, StringBuffer doc_Text)
	{

		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher=patternNumbersThousand.matcher(doc_Text);
		while (matcher.find())
		{
			addpatternNumbersThousandToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher=patternNumbersMillion.matcher(sb1);
		while (matcher.find())
		{
			addpatternNumbersMillionToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher=patternNumbersBillion.matcher(sb2);
		while (matcher.find())
		{
			addpatternNumbersBillionToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher=patternNumbersFraction.matcher(sb1);
		sb2.setLength(0);
		while (matcher.find())
		{
			addToHash(terms_Hash,matcher.group());
		}
		matcher.appendTail(sb2);


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
		while (matcher.find())
		{
			addpatternNumbersToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);


		return sb2;
	}

	//addpatternNumbersThousandToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersThousandToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match.subSequence(1,match.length()-"Thousand".length()-1).toString()+"K");
	}
	
	//addpatternNumbersMillionToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersMillionToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match.subSequence(1,match.length()-"Million".length()-1).toString()+"M");
	}
	
	//addpatternNumbersBillionToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersBillionToHash(HashMap<String,Integer> termsHash, String match )
	{
		addToHash(termsHash,match.subSequence(1,match.length()-"Billion".length()-1).toString()+"B");
	}

	//addpatternNumbersToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersToHash(HashMap<String,Integer> termsHash,String match )
	{
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

	//addpatternNumbersCommasToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersCommasToHash(HashMap<String,Integer> termsHash,String match )
	{
		match=removeComma(match);
		addpatternNumbersToHash(termsHash,match);
	}

	//parsePrices- input: stringBuffer of the text and hashMap to update
	//output:the new string buffer without the matches+ updates the the hash with the matched prices
	StringBuffer parsePrices(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{

		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher  = patternDollarSignMillion.matcher(doc_Text);//$price million
		while (matcher.find())
		{
			addpatternDollarSignMillionToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher  = patternDollarSignBillion.matcher(sb1);//$price billion
		while (matcher.find())
		{
			addpatternDollarSignBillionToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);
		sb1.setLength(0);

		matcher  = patternDollarSign.matcher(sb2);//$price
		while (matcher.find())
		{
			addpatternDollarSignToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternFractionDollar.matcher(sb1);//Price fraction Dollars
		while (matcher.find())
		{
			addpatternFractionDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternDollar.matcher(sb2);//Price Dollars
		while (matcher.find())
		{
			addpatternDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternMDollar.matcher(sb1);//Price m Dollars
		while (matcher.find())
		{
			addpatternMDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternBnDollar.matcher(sb2);//Price bn Dollars
		while (matcher.find())
		{
			addpatternBnDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternBillionUSDollar.matcher(sb1);//Price billion U.S. dollars
		while (matcher.find())
		{
			addpatternBillionUSDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternMillionUSDollar.matcher(sb2);//Price million U.S. dollars
		while (matcher.find())
		{
			addpatternMillionUSDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		sb2.setLength(0);
		matcher  = patternTrillionUSDollar.matcher(sb1);//Price trillion U.S. dollars
		while (matcher.find())
		{
			addpatternTrillionUSDollarToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		return sb2;
	}

	//addpatternDollarSignMillionToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction($price million -> price M Dollars)
	void addpatternDollarSignMillionToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match.subSequence(1,match.length()-"million".length()-1).toString()+" M Dollars");
	}

	//addpatternDollarSignBillionToHash- input: input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction ($price billion ->price M Dollars)
	void addpatternDollarSignBillionToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match.subSequence(1,match.length()-"billion".length()-1).toString()+"000"+" M Dollars");;
	}

	//addpatternDollarSignToHash- input: input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction($price -> above 1M- save as price M Dollars, else save as price Dollars)
	void addpatternDollarSignToHash(HashMap<String,Integer> termsHash,String match )
	{
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

	//addpatternFractionDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price fraction Dollars-> save the same)
	void addpatternFractionDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match);
	}

	//addpatternDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction (Price Dollars-> above 1M- save as price M Dollars, else save as price Dollars)
	void addpatternDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
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

	//addpatternMDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price m Dollars --> Price M Dollars)
	void addpatternMDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
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

	//addpatternBnDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price bn Dollars)
	void addpatternBnDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
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

	//addpatternBillionUSDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price billion U.S. dollars)
	void addpatternBillionUSDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
		match=removeComma(match);
		int idxspace=match.indexOf(" ");

		addToHash(termsHash,match.substring(0,idxspace)+"000 M Dollars");
	}

	//addpatternMillionUSDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price million U.S. dollars)
	void addpatternMillionUSDollarToHash(HashMap<String,Integer>termsHash, String match )
	{
		match=removeComma(match);
		int idxspace=match.indexOf(" ");

		addToHash(termsHash,match.substring(0,idxspace)+" M Dollars");
	}

	//addpatternTrillionUSDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price trillion U.S. dollars)
	void addpatternTrillionUSDollarToHash(HashMap<String,Integer> termsHash, String match )
	{
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
			addpatternDDMonthToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		matcher  = patternMonthDD.matcher(sb1);
		while (matcher.find())
		{
			addpatternMonthDDToHash(terms_Hash, matcher.group());
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		matcher  = patternMonthYear.matcher(sb2);
		while (matcher.find())
		{
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
