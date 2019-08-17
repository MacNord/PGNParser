
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PGNParserColumn {

//	private static final String TAB = "\t";
	private static final String LINE_SEPARATOR = System.lineSeparator();

//	private static final String TAB = "&emsp;";
//	private static final String LINE_SEPARATOR = "<br/>";

	public static void modify(File file) {

		try (InputStream fileStream = new FileInputStream(file);
				Reader decoder = new InputStreamReader(fileStream, "UTF-8");
				BufferedReader buffered = new BufferedReader(decoder);) {
			String line = "";

			StringBuffer buffer = new StringBuffer();

			while (line != null) {
				line = buffered.readLine();
				buffer.append(line + " ");
//				System.out.println(line);
			}

			String oneLIne = buffer.toString().replaceAll(LINE_SEPARATOR, " ");
//			StringBuffer oneLineB = new StringBuffer(
//					"<!DOCTYPE html><html lang=\\\"en\\\"><head><meta charset=\\\"utf-8\\\"></head><body>");
//
//			oneLineB.append(oneLIne);

			LinkedList<String> tokens = tokenize(oneLIne);

			int moveNumber = 2;
			int level = 0;

			LinkedList<HalfMove> allHalfMoves = createMoves(tokens, moveNumber, level);

			for (int i = 0; i < allHalfMoves.size() - 1; i++) {

				HalfMove currentA = allHalfMoves.get(i);
				HalfMove nextA = allHalfMoves.get(i + 1);

				LinkedList<HalfMove> toInsert = insertsBefore(currentA, nextA);

				allHalfMoves.addAll(i + 1, toInsert);

				if (currentA.isWhite() && (currentA.getPosition() + 1 != nextA.getPosition())) {
					// no black move for this white move
					allHalfMoves.add(i + 1, new HalfMove(currentA.getNumber(), false, ".", currentA.getLevel(), ""));
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

			StringBuilder all = new StringBuilder();
			for (ArrayList<HalfMove> cur : allStructured) {

				for (int i = 0; i < cur.size(); i++) {
					if (i == 0) {
						all.append(LINE_SEPARATOR);
						// number always here..
						all.append(String.format("%6s", cur.get(i).getNumber() + ". "));
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

//			Parses PGN Files and formats them into columns for better readability
			System.out.println(all);

		} catch (Exception e) {
			e.printStackTrace();
		}

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
			if (half.getPosition() == 0 && half.getNumber() != 1) {
				allStructured.add(currentLine);
				currentLine = new ArrayList<HalfMove>();
			}
			currentLine.add(half);
		}

		currentLine.add(new HalfMove(0, true, "end", 0, ""));
		// add last line too
		allStructured.add(currentLine);

		return allStructured;
	}

	private static LinkedList<HalfMove> insertsBefore(HalfMove current, HalfMove next) {

		LinkedList<HalfMove> toInsert = new LinkedList<HalfMove>();

		if ((current.getPosition() + 1 != next.getPosition())) {
			// there is always a gap in position for new level..
//          black alternate move
//			w0  b0
//			.   .   .  b1
//			white alternative move
//			w0  .
//			.   .   w1  b1

			// insert needed
			for (int i = 0; i < next.getPosition(); i++) {
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
