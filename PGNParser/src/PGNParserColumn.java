
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tutego.jrtf.Rtf;
import com.tutego.jrtf.RtfTextPara;

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

		LinkedList<HalfMove> allHalfMoves = createMoves(tokens, moveNumber, level);

		for (int i = 0; i < allHalfMoves.size() - 1; i++) {

			HalfMove currentA = allHalfMoves.get(i);
			HalfMove nextA = allHalfMoves.get(i + 1);
			
			setLastLevelOf(allHalfMoves, i, currentA, nextA);

			LinkedList<HalfMove> toInsert = insertsBefore(currentA, nextA);

			allHalfMoves.addAll(i + 1, toInsert);

			if (currentA.isWhite() && (currentA.getColumn() + 1 != nextA.getColumn() || (currentA.getNumber() != nextA.getNumber()))) {
				// no black move for this white move
				allHalfMoves.add(i + 1, new HalfMove(currentA.getNumber(), false, ".", currentA.getLevel(), ""));
				currentA.setLastOfLevel(true);
			}
		}

		ArrayList<ArrayList<HalfMove>> allStructured = createdStrucuted(allHalfMoves);

		for (ArrayList<HalfMove> cur : allStructured) {
			StringBuilder previousCommentsOfLine = new StringBuilder();
			for (int i = 0; i < cur.size(); i++) {
				if (i < cur.size() - 1) {
					previousCommentsOfLine.append(cur.get(i).getComment());
					cur.get(i).setComment("");
				} else {
//						add it
					cur.get(i).setComment(previousCommentsOfLine.toString() + cur.get(i).getComment());
				}
			}
		}

		StringBuilder all = printIt(pgnHeaderData, allStructured);
//			Parses PGN Files and formats them into columns for better readability
		System.out.println(all);

		System.out.println("first size is " + allStructured.size());
		return RTFPrinter.getParagraphs(pgnHeaderData, allStructured);
	}

	/**
	 * 
	 * @param allHalfMoves
	 * @param i
	 * @param currentA
	 * @param nextA
	 */
	private void setLastLevelOf(LinkedList<HalfMove> allHalfMoves, int i, HalfMove currentA, HalfMove nextA) {
		if (currentA.getLevel() != nextA.getLevel()) {
			boolean nextMoveExists = false;
			for (int j = i + 1; j < allHalfMoves.size(); j++) {
				HalfMove futureHalfMove = allHalfMoves.get(j);
				if (currentA.getLevel() == futureHalfMove.getLevel()
						&& currentA.getHalfMoveNumber() + 1 == futureHalfMove.getHalfMoveNumber()) {
					nextMoveExists = true;
					break;
				}
			}
			if (!nextMoveExists) {
				currentA.setLastOfLevel(true);
			}
		}
	}

	/**
	 * 
	 * @param allStructured
	 * @return
	 */
	private static StringBuilder printIt(ArrayList<String> header, ArrayList<ArrayList<HalfMove>> allStructured) {

		StringBuilder all = new StringBuilder();

		for (String headerLine : header) {
			all.append(headerLine);
			all.append(LINE_SEPARATOR);
		}

		for (ArrayList<HalfMove> cur : allStructured) {

			for (int i = 0; i < cur.size(); i++) {
				if (i == 0) {
					all.append(LINE_SEPARATOR);
					// number always here..
					all.append(String.format("%4s", cur.get(i).getNumber() + ". "));
				}
				all.append(String.format("%6s", cur.get(i).getHalfMove() + cur.get(i).getAttribute()) + " |");

				// comment is always on last item of line here..
				if (!cur.get(i).getComment().isEmpty()) {
					ArrayList<String> allComments = cur.get(i).getStructuredComments(35);
					for (String commentLine : allComments) {
						all.append(LINE_SEPARATOR);
						all.append("                                        c:|" + commentLine);
					}
				}

			}
		}
		return all;
	}

	/**
	 * Double dimension plus end elements.
	 * 
	 * @param allHalfMoves
	 */
	private static ArrayList<ArrayList<HalfMove>> createdStrucuted(LinkedList<HalfMove> allHalfMoves) {
		ArrayList<ArrayList<HalfMove>> allStructured = new ArrayList<ArrayList<HalfMove>>();

		ArrayList<HalfMove> currentLine = new ArrayList<HalfMove>();

		for (HalfMove half : allHalfMoves) {

			// line completed
			if (half.getColumn() == 0 && half.getNumber() != 1) {
				allStructured.add(currentLine);
				currentLine = new ArrayList<HalfMove>();
			}
			currentLine.add(half);
		}

		currentLine.add(new HalfMove(0, currentLine.size() % 2 == 0, "end", 0, "end"));
		// add last line too
		allStructured.add(currentLine);

		return allStructured;
	}

	private static LinkedList<HalfMove> insertsBefore(HalfMove current, HalfMove next) {

		LinkedList<HalfMove> toInsert = new LinkedList<HalfMove>();

		if ((current.getColumn() + 1 != next.getColumn())) {
			// there is always a gap in position for new level..
//          black alternate move
//			w0  b0
//			.   .   .  b1
//			white alternative move
//			w0  .
//			.   .   w1  b1

			// insert needed
			for (int i = 0; i < next.getColumn(); i++) {
				toInsert.add(new HalfMove(next.getNumber(), ((i % 2 == 0) ? true : false), ".", i / 2, ""));
			}
		}
		return toInsert;
	}

	private static void insertOne(LinkedList<HalfMove> allHalfMoves, int i, HalfMove nextA) {

		HalfMove dummy = new HalfMove(nextA.getNumber(), (i % 2 == 0 ? true : false), ".", i / 2, "");

		allHalfMoves.add(i + 1, dummy);
		System.out.println(Arrays.deepToString(allHalfMoves.toArray()));
	}

	/**
	 * 
	 * @param tokens
	 * @param moveNumber
	 * @param level
	 * @return
	 */
	private static LinkedList<HalfMove> createMoves(LinkedList<String> tokens, int moveNumber, int level) {
		LinkedList<HalfMove> halfMoves = new LinkedList<HalfMove>();

		for (String token : tokens) {
			if (token.startsWith("{")) {
				halfMoves.getLast().setComment(token);
			} else if (token.startsWith("+") || token.startsWith("?") || token.startsWith("!")
					|| token.startsWith("#")) {
				halfMoves.getLast().setAttribute(token);
			} else if (token.startsWith("(")) {

				String newLevel = token.replaceFirst("\\(", "");
				char last = newLevel.charAt(newLevel.length() - 1);
				if (last != ')') {
					System.err.print("last char was not )");
				} else {
					// trim last
					newLevel = newLevel.substring(0, newLevel.length() - 1);
					// System.out.println("level !" + newLevel);
				}

				// next same move number but new level
				halfMoves.addAll(createMoves(tokenize(newLevel), moveNumber - 1, level + 1));

			} else {
				// only increase move number if it is a move
				halfMoves.add(new HalfMove(moveNumber / 2, (moveNumber % 2 == 0 ? true : false), token, level, ""));
				moveNumber++;
			}
		}
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
