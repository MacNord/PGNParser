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
		
		all.add(p(fontSize(32, color(1, bold(shadow("exd5")))),fontSize(32, color(1, bold(shadow("aaa"))))));
		all.add(p(fontSize(32, color(1, bold(shadow("exd5"))))));

		return  all.toArray( new RtfTextPara[2]);
	}

//	public CharSequence printRTF(ArrayList<String> header, ArrayList<ArrayList<PGNTree>> allStructured) throws IOException {
//
//
//	}

	public static ArrayList<RtfTextPara> getParagraphs(ArrayList<String> header, ArrayList<ArrayList<PGNTree>> allStructured) {

		ArrayList<RtfTextPara> oneGame = new ArrayList<RtfTextPara>();
				
		for (String headerLine : header) {
			oneGame.add(p(fontSize(FONT_SIZE, font(1,headerLine))));
		}
	

		ArrayList<RtfText> currentRTFTextLine = new ArrayList<RtfText>();
		
		for (ArrayList<PGNTree> cur : allStructured) {

			for (int i = 0; i < cur.size(); i++) {
				if (i == 0) {
					oneGame.add(p(currentRTFTextLine.toArray( new RtfText[currentRTFTextLine.size()])));
					currentRTFTextLine = new ArrayList<RtfText>();

					// number always here..
					currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, String.format("%4s", cur.get(i).getNumber() + ". "))));
				}

				String PGNTree = String.format("%6s", cur.get(i).getHalfMove() + cur.get(i).getAttribute());
				
				if (cur.get(i).isLastOfLevel()) {
					if (cur.get(i).isWhite()) {
						currentRTFTextLine.add(fontSize(FONT_SIZE, bold(font(1, color(1, PGNTree)))));
					} else {
						currentRTFTextLine.add(fontSize(FONT_SIZE, bold(font(1, color(2, PGNTree)))));
					}
				} else {
					if (cur.get(i).isWhite()) {
						currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, color(1, PGNTree))));
					} else {
						currentRTFTextLine.add(fontSize(FONT_SIZE, font(1, color(2, PGNTree))));
					}
				}

				// comment is always on last item of line here..
//				if (!cur.get(i).getComment().isEmpty()) {
//					ArrayList<String> allComments = cur.get(i).getStructuredComments(38);
//					for (String commentLine : allComments) {
//						oneGame.add(p(currentRTFTextLine.toArray( new RtfText[currentRTFTextLine.size()])));
//						currentRTFTextLine = new ArrayList<RtfText>();
//						currentRTFTextLine.add(
//								fontSize(FONT_SIZE, font(1, color(3,"                                        " + commentLine))));
//					}
//				}
			}
		}
		System.out.println("size is " + oneGame.size());
		
//		StringBuilder all = new StringBuilder();
//		for (ArrayList<PGNTree> cur : allStructured) {
//
//			for (int i = 0; i < cur.size(); i++) {
//				if (i == 0) {
//					all.append(LINE_SEPARATOR);
//					// number always here..
//					all.append(String.format("%3s", cur.get(i).getNumber() + ". "));
//				}
//				all.append(String.format("%6s", cur.get(i).getPGNTree() + cur.get(i).getAttribute()) + " |");
//
//				// comment is always on last item of line here..
//				if (!cur.get(i).getComment().isEmpty()) {
//					ArrayList<String> allComments = cur.get(i).getStructuredComments(35);
//					for (String commentLine : allComments) {
//						all.append(LINE_SEPARATOR);
//						all.append("                                        c:|" + commentLine);
//					}
//				}
//
//			}
//		}
//		return all;

		return  oneGame;
	}

}
