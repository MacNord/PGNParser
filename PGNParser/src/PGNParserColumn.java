
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
import java.util.Collections;
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

		boolean doubleEndBrackets = false;
		if (tokens.getLast().startsWith("(") && tokens.getLast().endsWith("))")) {
			doubleEndBrackets = true;
		}
		
		PGNTree root = PGNTree.createRootNode();
		createMoves(tokens, root, moveNumber, level, doubleEndBrackets);
		
		LinkedList<PGNTree> allPGNTrees = new LinkedList<>();
		//TODO: deviation at fist child ?
		allPGNTrees = root.getChildren().getFirst().toFlatList(allPGNTrees);
		System.out.println("size is  " + allPGNTrees.size() );
		
		//sort moves
		Collections.sort(allPGNTrees);
		System.out.println("sorted");
		
		System.out.println(Arrays.toString(allPGNTrees.toArray()));
		
		ArrayList<ArrayList<PGNTree>> allStructured = createLines(allPGNTrees);

//		for (int i = 0; i < allPGNTrees.size() - 1; i++) {
//
//			PGNTree currentA = allPGNTrees.get(i);
//			PGNTree nextA = allPGNTrees.get(i + 1);
//			
//			LinkedList<PGNTree> toInsert = insertsBefore(currentA, nextA);
//
//			allPGNTrees.addAll(i + 1, toInsert);
//
//			if (currentA.isWhite() && (currentA.getColumn() + 1 != nextA.getColumn() || (currentA.getNumber() != nextA.getNumber()))) {
//				// no black move for this white move
//				allPGNTrees.add(i + 1, new PGNTree(currentA.getNumber(), false, ".", currentA.getLevel(), ""));
//				//currentA.setLastOfLevel(true);
//			}
//		}
//
//		ArrayList<ArrayList<PGNTree>> allStructured = createdStrucuted(allPGNTrees);
//
//		for (ArrayList<PGNTree> cur : allStructured) {
//			StringBuilder previousCommentsOfLine = new StringBuilder();
//			for (int i = 0; i < cur.size(); i++) {
//				if (i < cur.size() - 1) {
//					previousCommentsOfLine.append(cur.get(i).getComment());
//					cur.get(i).setComment("");
//				} else {
////						add it
//					cur.get(i).setComment(previousCommentsOfLine.toString() + cur.get(i).getComment());
//				}
//			}
//		}

		StringBuilder all = printIt(pgnHeaderData, allStructured);
//////			Parses PGN Files and formats them into columns for better readability
		System.out.println(all);
////
//		System.out.println("first size is " + allStructured.size());
		return RTFPrinter.getParagraphs(pgnHeaderData, allStructured);
//		return null;
	}
	
	/**
	 * 
	 * @param tokens
	 * @param moveNumber
	 * @param level
	 * @return
	 */
	private static PGNTree createMoves(LinkedList<String> tokens, PGNTree root , int moveNumber, int level, boolean doubleEndBrackets) {
		
		PGNTree newNode = root;
		PGNTree oldRoot;

		for (String token : tokens) {
			if (token.startsWith("{")) {
				root.setComment(token);
			} else if (token.startsWith("+") || token.startsWith("?") || token.startsWith("!")
					|| token.startsWith("#")) {
				root.setAttribute(token);
			} else if (token.startsWith("(")) {

				String newLevel = token.replaceFirst("\\(", "");
				System.out.println("token " + token);

				//check from last level
				if (doubleEndBrackets) {
					root.setLastOfLevel(true);
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
				
				oldRoot = root;
				// if () here next Node is not child of this root but of its parent... sibling of this root
				createMoves(tokenize(newLevel), root.getParent(), moveNumber - 1, level + 1, doubleEndBrackets);
				// root should remain the same as before...
				root = oldRoot;

			} else {
				// only increase move number if it is a move
				System.out.println("old" + newNode);
				newNode = PGNTree.createChildNode(root, moveNumber / 2, (moveNumber % 2 == 0 ? true : false), token, level, "");
				//normally root becomes the new node to link at
				root = newNode;
				
				System.out.println("new" + newNode);
				moveNumber++;
			}
		}
		
		newNode.setLastOfLevel(true);
		
		return newNode;
	}




	/**
	 * 
	 * @param allStructured
	 * @return
	 */
	private static StringBuilder printIt(ArrayList<String> header, ArrayList<ArrayList<PGNTree>> allStructured) {

		StringBuilder all = new StringBuilder();

		for (String headerLine : header) {
			all.append(headerLine);
			all.append(LINE_SEPARATOR);
		}

		for (ArrayList<PGNTree> cur : allStructured) {

			for (int i = 0; i < cur.size(); i++) {
				if (i == 0) {
					all.append(LINE_SEPARATOR);
					// number always here..
				all.append(String.format("%4s", cur.get(i).getNumber()));
				}
				
				//all.append(String.format("%4s", cur.get(i).getIndex()));

				all.append(String.format("%6s", cur.get(i).getHalfMove() + "/" + cur.get(i).getParent().getHalfMove() + cur.get(i).getAttribute()) + " |");

				// comment is always on last item of line here..
//				if (!cur.get(i).getComment().isEmpty()) {
//					ArrayList<String> allComments = cur.get(i).getStructuredComments(35);
//					for (String commentLine : allComments) {
//						all.append(LINE_SEPARATOR);
//						all.append("                                        c:|" + commentLine);
//					}
//				}

			}
		}
		return all;
	}
	
	/**
	 * Double dimension plus end elements.
	 * 
	 * @param allPGNTrees
	 */
	private static ArrayList<ArrayList<PGNTree>> createLines(LinkedList<PGNTree> allPGNTrees) {
		ArrayList<ArrayList<PGNTree>> allStructured = new ArrayList<ArrayList<PGNTree>>();
		ArrayList<PGNTree> currentLine = new ArrayList<PGNTree>();
		
		int previousHalfMoveNumber = -1;
		for (PGNTree half : allPGNTrees) {
			
			// line completed
			if (half.getHalfMoveNumber() != previousHalfMoveNumber) {
				allStructured.add(currentLine);
				currentLine = new ArrayList<PGNTree>();
			}
			currentLine.add(half);
			
			previousHalfMoveNumber = half.getHalfMoveNumber();
		}

		//currentLine.add(new PGNTree(0, currentLine.size() % 2 == 0, "end", 0, "end"));
		// add last line too
		allStructured.add(currentLine);

		return allStructured;
	}

	/**
	 * Double dimension plus end elements.
	 * 
	 * @param allPGNTrees
	 */
	private static ArrayList<ArrayList<PGNTree>> createdStrucuted(LinkedList<PGNTree> allPGNTrees) {
		ArrayList<ArrayList<PGNTree>> allStructured = new ArrayList<ArrayList<PGNTree>>();

		ArrayList<PGNTree> currentLine = new ArrayList<PGNTree>();

		for (PGNTree half : allPGNTrees) {

			// line completed
			if (half.getColumn() == 0 && half.getNumber() != 1) {
				allStructured.add(currentLine);
				currentLine = new ArrayList<PGNTree>();
			}
			currentLine.add(half);
		}

		currentLine.add(PGNTree.createRootNode());
		// add last line too
		allStructured.add(currentLine);

		return allStructured;
	}

	private static LinkedList<PGNTree> insertsBefore(PGNTree current, PGNTree next) {

		LinkedList<PGNTree> toInsert = new LinkedList<PGNTree>();

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
				toInsert.add(PGNTree.createRootNode());
			}
		}
		return toInsert;
	}

//	private static void insertOne(LinkedList<PGNTree> allPGNTrees, int i, PGNTree nextA) {
//
//		PGNTree dummy = new PGNTree(nextA.getNumber(), (i % 2 == 0 ? true : false), ".", i / 2, "");
//
//		allPGNTrees.add(i + 1, dummy);
//		System.out.println(Arrays.deepToString(allPGNTrees.toArray()));
//	}



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
