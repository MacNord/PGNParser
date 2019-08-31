import static com.tutego.jrtf.RtfHeader.color;
import static com.tutego.jrtf.RtfHeader.font;
import static com.tutego.jrtf.RtfPara.p;
import static com.tutego.jrtf.RtfText.bold;
import static com.tutego.jrtf.RtfText.color;
import static com.tutego.jrtf.RtfText.font;
import static com.tutego.jrtf.RtfText.fontSize;
import static com.tutego.jrtf.RtfText.shadow;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.tutego.jrtf.Rtf;
import com.tutego.jrtf.RtfText;
import com.tutego.jrtf.RtfTextPara;

public class RTFPrinter {

	private static final int FONT_SIZE = 18;

	public static void main(String[] args) throws IOException {

		Rtf.rtf()
				.header(color(185, 185, 185).at(1), color(2, 0xff, 0).at(2), color(3, 0, 0xff).at(3),
						font("Noto Mono").at(1))
				.section(extracted()).out(new FileWriter("out.rtf"));

	}

	private static RtfTextPara[] extracted() {

		ArrayList<RtfTextPara> all = new ArrayList<RtfTextPara>();
		
		all.add(p(fontSize(32, color(1, bold(shadow("exd5")))),fontSize(32, color(1, bold(shadow("aaa"))))));
		all.add(p(fontSize(32, color(1, bold(shadow("exd5"))))));
		return  all.toArray( new RtfTextPara[2]);
	}

	/**
	 * @param header
	 * @param tree
	 * @return
	 */
	public static ArrayList<RtfTextPara> getParagraphs(ArrayList<String> header, TreeMap<Location, PGNTree> tree) {

		ArrayList<RtfTextPara> oneGame = new ArrayList<RtfTextPara>();

		for (String headerLine : header) {
			oneGame.add(p(fontSize(FONT_SIZE, font(1, headerLine))));
		}

		ArrayList<RtfText> currentRTFTextLine = new ArrayList<RtfText>();
		
		ArrayList<ArrayList<RtfText>> allCommentsOfLine = new ArrayList<ArrayList<RtfText>>();
		
		ArrayList<RtfText> currentComment = new ArrayList<RtfText>();
		currentComment.add(fontSize(FONT_SIZE, font(1, "Comments: ")));
		allCommentsOfLine.add(currentComment);
		currentComment = new ArrayList<RtfText>();
		
		

		int line = -1;
		int posInLine = 0;
		RtfText halfM = null;
		RtfText lineNumber = null;
		
		for (Location loc : tree.keySet()) {

			PGNTree cur = tree.get(loc);

			if (cur != null) {

				if (line != loc.getY()) {
					// add line to game
					oneGame.add(p(currentRTFTextLine.toArray(new RtfText[currentRTFTextLine.size()])));
					
					// init fresh
					currentRTFTextLine = new ArrayList<RtfText>();
					
					
					lineNumber = fontSize(FONT_SIZE, font(1, String.format("%4s", cur.getNumber() + ".")));
					
					currentRTFTextLine.add(lineNumber);
					posInLine = 0;
				}

				while (posInLine < loc.getX()) {
					currentRTFTextLine.add(fontSize(FONT_SIZE, String.format("%6s", "_") + " "));
					posInLine++;
				}

				String halfMove = String.format("%6s", cur.getHalfMove() + cur.getAttribute());


				if (cur.isLastOfLevel()) {
					if (cur.isWhite()) {
						halfM = fontSize(FONT_SIZE, bold(font(1, color(1, halfMove))));
					} else {
						halfM = fontSize(FONT_SIZE, bold(font(1, color(2, halfMove))));
					}
				} else {
					if (cur.isWhite()) {
						halfM = fontSize(FONT_SIZE, font(1, color(1, halfMove)));
					} else {
						halfM = fontSize(FONT_SIZE, font(1, color(2, halfMove)));
					}
				}
				
				
				if (!cur.getComment().isEmpty()) {
					
					currentComment.add(lineNumber);
					currentComment.add(halfM);
					currentComment.add(fontSize(FONT_SIZE, font(1, color(3,  ":" + cur.getComment()))));
					allCommentsOfLine.add(currentComment);
					currentComment = new ArrayList<RtfText>();
					
					currentRTFTextLine.add(halfM);
					currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, color(3,  "*"))));
				} else {
					currentRTFTextLine.add(halfM);
					//no comment
					currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, color(3,  " "))));
				}
				

				posInLine++;

				line = loc.getY();
			}
		}
		// add last line
		oneGame.add(p(currentRTFTextLine.toArray(new RtfText[currentRTFTextLine.size()])));
		allCommentsOfLine.add(currentComment);
		
		for (ArrayList<RtfText> comLine : allCommentsOfLine) {
			oneGame.add(p(comLine.toArray(new RtfText[comLine.size()])));
		}

		System.out.println("size is " + oneGame.size());

		return oneGame;
	}
	


}
