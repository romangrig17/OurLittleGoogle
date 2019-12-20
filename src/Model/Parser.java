package Model;

import java.time.LocalTime;
import java.util.regex.Pattern;

import javafx.util.Pair;

import java.util.HashMap;
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
	Pattern patternPercent = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ]{1})?(percentage|percent|%)");//Number percent//Number percentage

	//Date
	Pattern patternDate = Pattern.compile("([0-3]{1}[0-9]{1}[ ])?(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)[ ]([0-3]{1}[0-9]{1}\\D|[1-2]{1}[0-9]{1}[0-9]{1}[0-9]{1}\\D)?");//DD Month//Month DD//Month year

	//Price
	Pattern patternDollarSignBillionMillion = Pattern.compile("\\$(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?([ ](billion|million))?"
			+ "|\\$\\d+\\.?\\d*([ ](billion|million))?");//$price billion/$price million
	Pattern patternPrice = Pattern.compile("((\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\[0-9]{1,})?|(\\d{1,})(\\.\\d{1,})?)( Dollars| dollars|m Dollars|bn Dollars| billion U.S. dollars| million U.S. dollars| trillion U.S. dollars)"
			+"|((\\d{1,3},\\d{3}(,\\d{3})*)|(\\d{1,6}))( (\\d{1,}+\\/+\\d{1,}))?( Dollars| dollars)");

	//expressions- (Word?)([-])?(number|word)[-](number|word)([-])?(word?)
	Pattern patternExpression= Pattern.compile("([A-z]{1,}[-])?([A-z]{1,}|(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?|[0-9]{1,}([\\\\.][0-9]{1,})?)[-]([A-z]{1,}|(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?|[0-9]{1,}([\\\\.][0-9]{1,})?)([-][A-z]{1,})?"); //Word-word-word/Word-word/Number-word/Word-Number/Number-number
	Pattern patternBetweenNumberAndNumber= Pattern.compile("(Between |between )\\d{1,}( and )\\d{1,}");//Between number and number

	//numbers
	Pattern patternNumbers = Pattern.compile("((\\d{1,3},\\d{3}(,\\d{3})*)|([0-9]{1,}))(([\\.][0-9]{1,})|( (\\d{1,}+\\/+\\d{1,})))?");//numbers-(with comma\just numbers\with dot) (fractions)?
	Pattern patternNumbersThoMilBil = Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ]{1,})(Thousand|Million|Billion)");

	//Names
	Pattern patternEntity= Pattern.compile("[A-Z]{1}[a-z]{1,}[ ][A-Z]{1,}[a-z]{1,}([ |-][A-Z]{1}[a-z]{1,}([ |-][A-Z]{1,}[a-z]{1,})?)?");

	//KG
	Pattern patternKG= Pattern.compile("[0-9]{1,}([\\.][0-9]{1,})?([ ])?(KG|kg|Kg|kilogram)");

	//phone number
	Pattern patternPhoneNumber= Pattern.compile("([(])?[0-9]{3}(-| )[0-9]{3}(-| )?[0-9]{4}([)])?");



	//This function parse the input doc_text.
	//The output is a hashMap of all the parsed words:
	//first string is the term,int is the count.
	public HashMap<String,Integer> parseDoc(String doc_Text, String doc_Number)
	{
		HashMap<String,Integer> termsHash= new HashMap<>();
		StringBuffer sb1 = new StringBuffer(doc_Text);

		sb1 = parseNames(termsHash,sb1);
		sb1=parsePercent(termsHash, sb1);
		sb1=parseDate(termsHash,sb1);
		sb1=parsePrices(termsHash,sb1);
		sb1=parsePhoneNumber(termsHash,sb1);
		sb1=parseExpressions(termsHash,sb1);
		sb1=parseKG(termsHash,sb1);
		sb1=parseNumbers(termsHash,sb1);
		parseEndWords(termsHash,sb1);
		//printHash(termsHash);

		return termsHash;
	}

	//parsePhoneNumber- input: stringBuffer of the text and hashMap to update
	//output:the new string buffer without the matches+ updates the the hash with the matched numbers
	//TODO- add as pattern
	StringBuffer parsePhoneNumber(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		Matcher matcher  = patternPhoneNumber.matcher(doc_Text);
		boolean matched = false;
		while (matcher.find())
		{
			matched = true;
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, "");
		}

		if (matched)
		{
			matcher.appendTail(sb1);
			doc_Text.setLength(0);
			return sb1;
		}

		return doc_Text;
	}

	//parseKG- input: stringBuffer of the text and hashMap to update
	//output:the new string buffer without the matches+ updates the the hash with the matched kilograms
	StringBuffer parseKG(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer() ;
		Matcher matcher  = patternKG.matcher(doc_Text);
		boolean matched=false;
		while (matcher.find())
		{
			matched=true;
			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, "");
		}
		if(matched)
		{
			matcher.appendTail(sb1);
			doc_Text.setLength(0);
			return sb1;
		}

		return doc_Text;
	}

	//parseNames- input: stringBuffer of the text and hashMap to update
	//output StringBuffer without the terms and  updates the the hash with the matched names and entities
	StringBuffer parseNames(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer() ;
		Matcher matcher  = patternEntity.matcher(doc_Text);
		while (matcher.find())
		{
			addToHash(terms_Hash,"!"+matcher.group());
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);

		doc_Text.setLength(0);
		return sb1;
	}

	//parseEndWords- input: stringBuffer of the text and hashMap to update
	//no output, just updates the the hash with the matched names and entities
	void parseEndWords(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		String[] allWords = doc_Text.toString().trim().replaceAll("\\(|\\)|\\]|\\[|\\\\|\\/|,|:|\\$|\\.|\\+|\\*|-|'|'|_|`|!|@|#|%|\\^|\"|&|;|\\?", "").split(" ");
		int allWordsLength = allWords.length;
		for(int i = 0; i<allWordsLength; i++)
		{

			if(allWords[i].matches("[A-z]{2,}"))
			{
				addToHash(terms_Hash,allWords[i]);
			}
		}

		doc_Text.setLength(0);
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
		StringBuffer sb1 = new StringBuffer(),sb2 = new StringBuffer();

		Matcher matcher=patternExpression.matcher(doc_Text);
		while (matcher.find())
		{
			String[] allWords = matcher.group().toString().split("-");
			for (int i=0; i<allWords.length; i++)
			{
				if(Character.isDigit(allWords[i].charAt(0)) )//adding number too
				{
					addToHash(terms_Hash,allWords[i]);
				}
			}

			addToHash(terms_Hash,matcher.group());
			matcher.appendReplacement(sb1, " ");
		}

		matcher.appendTail(sb1);
		matcher=patternBetweenNumberAndNumber.matcher(sb1);
		while (matcher.find())
		{
			String[] allWords = matcher.group().toString().split(" ");
			addToHash(terms_Hash,allWords[1]);
			addToHash(terms_Hash,allWords[3]);
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		doc_Text.setLength(0);
		return sb2;
	}

	//parseNumbers: input- hash and doc_text
	//output: add the matched numbers to the hash table and returns the new text without the matches
	StringBuffer parseNumbers(HashMap<String,Integer>terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher=patternNumbersThoMilBil.matcher(doc_Text);
		while (matcher.find())
		{
			String[] allWords = matcher.group().toString().split(" ");
			if(allWords[1].equals("Thousand"))
			{
				addToHash(terms_Hash,allWords[0]+"K");
			}
			else if(allWords[1].equals("Million"))
			{
				addToHash(terms_Hash,allWords[0]+"M");
			}
			else
			{
				addToHash(terms_Hash,allWords[0]+"B");
			}
			matcher.appendReplacement(sb1, "");
		}
		matcher.appendTail(sb1);

		matcher=patternNumbers.matcher(sb1);

		while (matcher.find())
		{
			String[] allWords = matcher.group().toString().split(" ");
			if(allWords.length==2)//fraction- add as is
			{
				addToHash(terms_Hash,matcher.group());
			}
			else
			{
				addpatternNumbersToHash(terms_Hash,matcher.group());
			}
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		doc_Text.setLength(0);

		return sb2;
	}

	//addpatternNumbersToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersToHash(HashMap<String,Integer> termsHash,String match )
	{
		Pair<types,Float> pair = checkNumSize(match);
		types type=pair.getKey();
		Float num=pair.getValue();
		match= upToThreeNubersAfterDot(match);
		if( type == types.NUMBER_SMALLER_THAN_1K)
		{
			addToHash(termsHash,match);
		}
		else if(type == types.NUMBER_1K_TO_1M )
		{
			num=num/1000;
			addToHash(termsHash,num.toString()+"K");
		}
		else if(type == types.NUMBER_1M_TO_1B)
		{
			num=num/1000000;
			addToHash(termsHash,String.format("%.3f", num)+"M");
		}
		else if(type == types.NUMBER_GREATER_THAN_1B)
		{
			num=num/1000000000;
			addToHash(termsHash,String.format("%.3f", num)+"B");
		}
	}

	//addpatternNumbersCommasToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction
	void addpatternNumbersCommasToHash(HashMap<String,Integer> termsHash,String match )
	{
		addpatternNumbersToHash(termsHash,match);
	}

	float getNumAsFloat(String num)
	{
		if(num.contains(","))
			num=removeComma(num);

		return Float.parseFloat(num);
	}


	//parsePrices- input: stringBuffer of the text and hashMap to update
	//output:the new string buffer without the matches+ updates the the hash with the matched prices
	StringBuffer parsePrices(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		Matcher matcher  = patternDollarSignBillionMillion.matcher(doc_Text);//$price (million/bilion)?
		while (matcher.find())
		{
			String match= matcher.group();
			String[] allWords = matcher.group().toString().split(" ");

			if(allWords.length==1)
			{
				addpatternDollarSignToHash(terms_Hash, match.substring(1,match.length()));
			}
			else if(allWords[1].equals("million"))
			{
				addToHash(terms_Hash,allWords[0].substring(1)+" M Dollars");
			}
			else//billion
			{
				addToHash(terms_Hash,(int)(getNumAsFloat(allWords[0].substring(1))*1000)+" M Dollars");
			}
			matcher.appendReplacement(sb1, " ");
		}

		matcher.appendTail(sb1);

		matcher  = patternPrice.matcher(sb1);//Price fraction Dollars
		while (matcher.find())
		{
			String[] allWords = matcher.group().toString().split(" ");
			String num= removeComma(allWords[0]);
			if(allWords.length== 4)//Price billion U.S. dollars/Price million U.S. dollars/Price trillion U.S. dollars
			{
				switch (allWords[1])
				{
					case "billion":
						addToHash(terms_Hash,num+"000 M Dollars");
						break;
					case "million":
						addToHash(terms_Hash,allWords[0]+" M Dollars");
						break;
					case "trillion":
						addToHash(terms_Hash,num+"000000 M Dollars");
						break;
				}
			}
			else if(allWords.length== 3)//Price fraction Dollars
			{
				if( checkNumSize(allWords[0]).getKey() == types.NUMBER_1K_TO_1M ||checkNumSize(allWords[0]).getKey() == types.NUMBER_SMALLER_THAN_1K )
				{
					addToHash(terms_Hash,matcher.group());
				}
			}
			else if(allWords.length== 2)//Price[m] Dollars/Price[bn] Dollars/Price Dollars(more+less than million dollar )
			{
				if(allWords[0].endsWith("m"))
				{
					addpatternMDollarToHash(terms_Hash,allWords[0]);
				}
				else if(allWords[0].endsWith("bn"))
				{
					addpatternBnDollarToHash(terms_Hash,allWords[0]);
				}
				else
				{
					addpatternDollarSignToHash(terms_Hash,allWords[0]);
				}
			}
			matcher.appendReplacement(sb2, " ");
		}
		matcher.appendTail(sb2);

		sb1.setLength(0);
		doc_Text.setLength(0);

		return sb2;
	}

	//addpatternDollarSignToHash- input: input:string number and hashMap to update
	//output: add the match to the hash according to the instruction($price -> above 1M- save as price M Dollars, else save as price Dollars)
	void addpatternDollarSignToHash(HashMap<String,Integer> termsHash,String match )
	{
		Pair<types,Float> pair = checkNumSize(match);
		types type=pair.getKey();
		float num=pair.getValue();
		if( type == types.NUMBER_GREATER_THAN_1B || type == types.NUMBER_1M_TO_1B)
		{
			addToHash(termsHash,(int)(num/1000000)+" M Dollars");
		}
		else if(type == types.NUMBER_1K_TO_1M || type == types.NUMBER_SMALLER_THAN_1K)
		{
			addToHash(termsHash,match.substring(0,match.length())+" Dollars");
		}
	}

	//addpatternMDollarToHash- input: the match (just the number+m) term and hashMap to update
	//output: add the match to the hash according to the instruction(Price m Dollars --> Price M Dollars)
	void addpatternMDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match.substring(0,match.length()-1)+" M Dollars");
	}

	//addpatternBnDollarToHash- input: the matched term and hashMap to update
	//output: add the match to the hash according to the instruction(Price bn Dollars)
	void addpatternBnDollarToHash(HashMap<String,Integer> termsHash,String match )
	{
		addToHash(termsHash,match.substring(0,match.length()-2)+"000 M Dollars");
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

		return num;
	}

	//input : hash and text
	//looking for a Percent match, adding the matches to the hashMap according to instructions
	StringBuffer parsePercent(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();

		Matcher matcher  = patternPercent.matcher(doc_Text);
		while (matcher.find())
		{
			int space= (matcher.group().contains(" ") == true)? 1:0;
			if(matcher.group().endsWith("%"))//ends with %
			{
				addToHash(terms_Hash, matcher.group().substring(0, matcher.group().length()-space-1)+"%");
			}
			else if(matcher.group().endsWith("percent"))//percent
			{
				addToHash(terms_Hash, matcher.group().substring(0, matcher.group().length()-space-"percent".length())+"%");
			}
			else//Percentage
			{
				addToHash(terms_Hash, matcher.group().substring(0,matcher.group().length()-space-"Percentage".length())+"%");
			}
			matcher.appendReplacement(sb1, " ");
		}
		matcher.appendTail(sb1);

		doc_Text.setLength(0);

		return sb1;
	}

	//input : hash and text
	//looking for a Date match, adding the matches to the hashMap
	StringBuffer parseDate(HashMap<String,Integer> terms_Hash, StringBuffer doc_Text)
	{
		StringBuffer sb1 = new StringBuffer();

		Matcher matcher  = patternDate.matcher(doc_Text);
		while (matcher.find())
		{
			String[] allWords = matcher.group().toString().split(" ");
			if(allWords.length>2)
			{
				if((allWords.length==2 || allWords[2].length()<4) && allWords[0].length()==2)//DD Month
				{
					addToHash(terms_Hash,MonthToNum(allWords[1])+"-"+allWords[0]);
				}
				else if(allWords[1].length()==2)//Month DD
				{
					addToHash(terms_Hash,MonthToNum(allWords[0])+"-"+allWords[1]);
				}
				else if(allWords.length==2) //MonthYear
				{
					addToHash(terms_Hash,allWords[1].substring(0, 4)+"-"+MonthToNum(allWords[0]));
				}
				else //full date- DD MM YY
				{
					String month=MonthToNum(allWords[1]);
					addToHash(terms_Hash,month+"-"+allWords[0]);
					addToHash(terms_Hash,allWords[2].substring(0,4)+"-" +month);
				}
			}

			matcher.appendReplacement(sb1, " ");
		}

		matcher.appendTail(sb1);

		doc_Text.setLength(0);

		return sb1;
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

	//input- string number
	//output- pair- the number as a float and hes type from the enum
	private Pair<types,Float> checkNumSize(String number)
	{
		types ret=types.UNDEFINED;
		float num=0;
		if(number.contains(","))
			number=removeComma(number);

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

		return 	new Pair<types,Float>(ret,num);
	}
}
