import java.awt.List;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.ListSelectionEvent;

import com.tutego.jrtf.Rtf;
import com.tutego.jrtf.RtfPara;
import com.tutego.jrtf.RtfText;
import com.tutego.jrtf.RtfTextPara;

import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfDocfmt.*;
import static com.tutego.jrtf.RtfHeader.*;
import static com.tutego.jrtf.RtfInfo.*;
import static com.tutego.jrtf.RtfFields.*;
import static com.tutego.jrtf.RtfPara.*;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.*;
import static com.tutego.jrtf.RtfText.*;
import static com.tutego.jrtf.RtfUnit.*;

public class RTFPrinter {

	private static final int FONT_SIZE = 18;

	public static void main(String[] args) throws IOException {

		Rtf.rtf().header(color(185, 185, 185).at(1), color(2, 0xff, 0).at(2), color(3, 0, 0xff).at(3),
				font("Noto Mono").at(1)).section(extracted()).out(new FileWriter("out.rtf"));

//		 Rtf.rtf().p( "Hello World" ).out( new FileWriter("out.rtf") );
//		 
//		 rtf().section(
//				   p( "first paragraph" ),
//				   p( tab(),
//				      " second par ",
//				      bold( "with something in bold" ),
//				      text( " and " ),
//				      italic( underline( "italic underline" ) )     
//				    )  
//				).out( out );

	}

	private static RtfTextPara[] extracted() {

		ArrayList<RtfTextPara> all = new ArrayList<RtfTextPara>();

		all.add(p(fontSize(32, color(1, bold(shadow("exd5")))), fontSize(32, color(1, bold(shadow("aaa"))))));
		all.add(p(fontSize(32, color(1, bold(shadow("exd5"))))));

		return all.toArray(new RtfTextPara[2]);
	}

	public static ArrayList<RtfTextPara> getParagraphs(ArrayList<String> header, ArrayList<PGNLine> allStructured) {

		ArrayList<RtfTextPara> oneGame = new ArrayList<RtfTextPara>();

		for (String headerLine : header) {
			oneGame.add(p(fontSize(FONT_SIZE, font(1, headerLine))));
		}

		for (PGNLine cur : allStructured) {

			ArrayList<RtfText> currentRTFTextLine = new ArrayList<RtfText>();
			
			// line number
			for (int j = 0; j < cur.currentLine.length; j++) {
				if (cur.currentLine[j] != null) {
					currentRTFTextLine.add(fontSize(FONT_SIZE,
							font(1, String.format("%4s", cur.currentLine[j].getNumber() + ". "))));
					break;
				}
			}

			boolean afterHalfMove = false;
			for (int i = 0; i < cur.currentLine.length; i++) {
			
				if (cur.currentLine[i] == null) {
					if (!afterHalfMove) {
						String blankMove;
						if (cur.openLevel[i / 2]) {
							blankMove = String.format("%6s", ".");
						} else {
							blankMove = String.format("%6s", " ");
						}
						currentRTFTextLine.add(fontSize(FONT_SIZE, bold(font(1, color(1, blankMove)))));
					}
					// insert blank
				} else {
					
					afterHalfMove = true;

					String halfMove = String.format("%6s",
							cur.currentLine[i].getHalfMove() + cur.currentLine[i].getAttribute());

					if (cur.currentLine[i].isLastOfLevel()) {
						if (cur.currentLine[i].isWhite()) {
							currentRTFTextLine.add(fontSize(FONT_SIZE, bold(font(1, color(1, halfMove)))));
						} else {
							currentRTFTextLine.add(fontSize(FONT_SIZE, bold(font(1, color(2, halfMove)))));
						}
					} else {
						if (cur.currentLine[i].isWhite()) {
							currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, color(1, halfMove))));
						} else {
							currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, color(2, halfMove))));
						}
					}
				}
			}

//			if (!cur.getCommentOfLine().toString().isEmpty()) {
//				ArrayList<String> allComments = getStructuredComments(cur.getCommentOfLine().toString(), 38);
//				for (String commentLine : allComments) {
//					oneGame.add(p(currentRTFTextLine.toArray(new RtfText[currentRTFTextLine.size()])));
//					currentRTFTextLine = new ArrayList<RtfText>();
//					currentRTFTextLine.add(fontSize(FONT_SIZE,
//							font(1, color(3, "                                        " + commentLine))));
//				}
//			}
			oneGame.add(p(currentRTFTextLine.toArray(new RtfText[currentRTFTextLine.size()])));
		}
		System.out.println("size is " + oneGame.size());

		return oneGame;
	}

	/**
	 * Splits comments
	 * 
	 * @param maxLenght
	 * @return
	 */
	public static ArrayList<String> getStructuredComments(String comment, int maxLenght) {

		String[] commentToken = comment.replace("{", " ").replace("}", " ").split(" ");

		StringBuilder commentLine = new StringBuilder();
		ArrayList<String> all = new ArrayList<String>();

		for (int i = 0; i < commentToken.length; i++) {
			String word = commentToken[i];

			if (!word.isEmpty()) {
				if ((commentLine.length() + word.length() + 1) > maxLenght) {
					all.add(commentLine.toString());
					commentLine.setLength(0);
				}
				commentLine.append(word);
				commentLine.append(" ");
			}
		}
		// last line
		all.add(commentLine.toString());

		return all;
	}

}
