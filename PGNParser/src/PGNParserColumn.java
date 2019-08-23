
import static com.tutego.jrtf.RtfHeader.color;

import static com.tutego.jrtf.RtfHeader.font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tutego.jrtf.Rtf;
import com.tutego.jrtf.RtfTextPara;

/**
 * Parses PGN Files and formats them into columns for better readability
 * @author casanova
 */

public class PGNParserColumn {

//	private static final String TAB = "\t";
	private static final String LINE_SEPARATOR = System.lineSeparator();

//	private static final String TAB = "&emsp;";
//	private static final String LINE_SEPARATOR = "<br/>";

	public void modify(File inPutPGNFile) {

		try(FileWriter outPutFileWriter = new FileWriter(inPutPGNFile.getName() + ".rtf")) {

			try (InputStream fileStream = new FileInputStream(inPutPGNFile);
					Reader decoder = new InputStreamReader(fileStream, "UTF-8");
					BufferedReader buffered = new BufferedReader(decoder);) {
				String line = "";

				StringBuffer pgnMoveData = new StringBuffer();
				ArrayList<String> pgnHeaderData = new ArrayList<String>();

				boolean newPgn = false;

				ArrayList<RtfTextPara> allRTF = new ArrayList<RtfTextPara>();

				while (line != null) {
					line = buffered.readLine();

					if (line != null && line.startsWith("[")) {
						if (!newPgn) {
							if (!pgnHeaderData.isEmpty()) {
								// new file process so far data
								allRTF.addAll(processGame(pgnMoveData, pgnHeaderData));
							}
							// next game
							pgnHeaderData = new ArrayList<String>();
						}
						pgnHeaderData.add(line);
						newPgn = true;
					} else {
						if (newPgn) {
							pgnMoveData.setLength(0);
						}
						pgnMoveData.append(line + " ");
						newPgn = false;
					}
				}
				// process last game as well
				allRTF.addAll(processGame(pgnMoveData, pgnHeaderData));

				// print the bloody rtf :)
				Rtf.rtf().header(color(185, 0, 0).at(1), color(0, 0, 0).at(2), color(15, 128, 255).at(3),
						font("Noto Mono").at(1)).section(allRTF.toArray( new RtfTextPara[allRTF.size()])).out(outPutFileWriter);
				

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * 
	 * @param outPutFileWriter
	 * @param pgnMoveData
	 * @param pgnHeaderData
	 * @throws IOException
	 */
	private ArrayList<RtfTextPara> processGame(StringBuffer pgnMoveData, ArrayList<String> pgnHeaderData)
			throws IOException {
		String oneLIne = pgnMoveData.toString().replaceAll(LINE_SEPARATOR, " ");

		LinkedList<String> tokens = tokenize(oneLIne);

		int moveNumber = 2;
		int level = 0;

		boolean doubleEndBrackets = false;
		if (tokens.getLast().startsWith("(") && tokens.getLast().endsWith("))")) {
			doubleEndBrackets = true;
		}
		
		LinkedList<HalfMove> allHalfMoves = createMoves(tokens, moveNumber, level, doubleEndBrackets);

		Integer maxLevel = allHalfMoves.stream().mapToInt(HalfMove::getLevel).max().getAsInt();
		maxLevel = maxLevel + 1; //since level starts with 0
		System.out.println("maxLevel " + maxLevel);

		ArrayList<PGNLine> allStructured = createdStrucuted(allHalfMoves, maxLevel);

		for (PGNLine line : allStructured) {
			System.out.println(Arrays.toString(line.openLevel));
			System.out.println(Arrays.toString(line.getCurrentLine()));
		}

		StringBuilder all = printIt(pgnHeaderData, allStructured);

		System.out.println(all);

		System.out.println("first size is " + allStructured.size());
		return RTFPrinter.getParagraphs(pgnHeaderData, allStructured);
	

	}




	/**
	 * 
	 * @param allStructured
	 * @return
	 */
	private static StringBuilder printIt(ArrayList<String> header, ArrayList<PGNLine> allStructured) {

		StringBuilder all = new StringBuilder();

		for (String headerLine : header) {
			all.append(headerLine);
			all.append(LINE_SEPARATOR);
		}

		for (PGNLine cur : allStructured) {
			for (int i = 0; i < cur.getCurrentLine().length; i++) {
				HalfMove currHalfMove = cur.getCurrentLine()[i];

				if (currHalfMove != null) {

					if (i == 0) {
						all.append(LINE_SEPARATOR);
						// number always here..
						all.append(String.format("%4s", currHalfMove.getNumber() + ". "));
					}
					all.append(String.format("%6s", currHalfMove.getHalfMove() + currHalfMove.getAttribute()) + " |");
				}
			}
			// comment is always on last item of line here..
			all.append(LINE_SEPARATOR);
			all.append("                                        c:|" + cur.getCommentOfLine());
		}
		return all;
	}

	/**
	 * Double dimension plus end elements.
	 * 
	 * @param allHalfMoves
	 */
	private static ArrayList<PGNLine> createdStrucuted(LinkedList<HalfMove> allHalfMoves, int maxLevel) {
		
		ArrayList<PGNLine> allStructured = new ArrayList<PGNLine>();

		boolean[] openLevel = new boolean[maxLevel];
		Arrays.fill(openLevel, Boolean.FALSE);
		PGNLine currentLine = new  PGNLine(maxLevel*2, Arrays.copyOf(openLevel, openLevel.length));

		int previousColumn = 0;
		boolean previousLastOfLevel = false;
		int previousLevel = 0;
		
		for (HalfMove half : allHalfMoves) {
			
			openLevel[half.getLevel()] = true;

			// line completed
			if (half.getColumn() < previousColumn  || previousLastOfLevel || previousLevel != half.getLevel()) {
				
				allStructured.add(currentLine);
				currentLine = new  PGNLine(maxLevel*2,  Arrays.copyOf(openLevel, openLevel.length));
				
			}
			
			if (half.isLastOfLevel()) {
				openLevel[half.getLevel()] = false;
			}
			
			//TODO Create Setters
			currentLine.getCommentOfLine().append(half.getComment() + " ");
			currentLine.getCurrentLine()[half.getColumn()] =  half;
			previousColumn = half.getColumn();
			previousLastOfLevel = half.isLastOfLevel();
			previousLevel = half.getLevel();
			 
		}

		// add last line too
		allStructured.add(currentLine);

		return allStructured;
	}



	/**
	 * 
	 * @param tokens
	 * @param moveNumber
	 * @param level
	 * @return
	 */
	private static LinkedList<HalfMove> createMoves(LinkedList<String> tokens, int moveNumber, int level, boolean doubleEndBrackets) {
		LinkedList<HalfMove> halfMoves = new LinkedList<HalfMove>();
		

		for (String token : tokens) {
			if (token.startsWith("{")) {
				halfMoves.getLast().setComment(token);
			} else if (token.startsWith("+") || token.startsWith("?") || token.startsWith("!")
					|| token.startsWith("#")) {
				halfMoves.getLast().setAttribute(token);
			} else if (token.startsWith("(")) {

				String newLevel = token.replaceFirst("\\(", "");
				System.out.println("token " + token);

				//check from last level
				if (doubleEndBrackets) {
					halfMoves.getLast().setLastOfLevel(true);
				}
				
				// for next level
				if (token.endsWith("))")) {
					doubleEndBrackets = true;
				}
				
				char last = newLevel.charAt(newLevel.length() - 1);
				if (last == ')') {
					// trim last
					newLevel = newLevel.substring(0, newLevel.length() - 1);
					// System.out.println("level !" + newLevel);
				}

				// next same move number but new level
				halfMoves.addAll(createMoves(tokenize(newLevel), moveNumber - 1, level + 1, doubleEndBrackets));

			} else {
				// only increase move number if it is a move
				halfMoves.add(new HalfMove(moveNumber / 2, (moveNumber % 2 == 0 ? true : false), token, level, ""));
				moveNumber++;
			}
		}
		
		halfMoves.getLast().setLastOfLevel(true);
		
		return halfMoves;
	}

	private static LinkedList<String> tokenize(String oneLIne) {
		String regex = "\\{.*?\\}|\\(|\\)|[BRQNK][a-h][1-8]| [a-h][1-8]|[BRQNK][a-h][a-h][1-8]|O-O|0-0-0|[BRQNK]x[a-h][1-8]|[a-h]x[a-h][1-8]|[\\+|\\?|\\!|\\#]?[\\+|\\?|\\!||\\#]|1\\/2-1\\/2|1\\/-O|O-\\/1";
		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(oneLIne);

		LinkedList<String> tokens = new LinkedList<String>();

		StringBuilder tokenBuilder = new StringBuilder();

		int bracketCounter = 0;

		while (matcher.find()) {
			String token = matcher.group();

			// System.out.println("found token " + token);

			if (token.equals("(")) {
				bracketCounter++;
			} else if (token.equals(")")) {
				bracketCounter--;
			}

			tokenBuilder.append(token);

			if (bracketCounter == 0) {
				// done
				tokens.add(tokenBuilder.toString());
				tokenBuilder.setLength(0);
			}

		}
		return tokens;
	}

}
