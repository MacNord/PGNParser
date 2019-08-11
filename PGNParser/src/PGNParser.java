
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class PGNParser {

//	private static final String TAB = "\t";
//	private static final String LINE_SEPARATOR = System.lineSeparator();

	private static final String TAB = "&emsp;";
	private static final String LINE_SEPARATOR = "<br/>";

	public static void modify(File file) {

		try (InputStream fileStream = new FileInputStream(file);
				Reader decoder = new InputStreamReader(fileStream, "UTF-8");
				BufferedReader buffered = new BufferedReader(decoder);) {
			String line = "";

			StringBuffer buffer = new StringBuffer();

			while (line != null) {
				line = buffered.readLine();
				buffer.append(line);
				System.out.println(line);
			}

			String oneLIne = buffer.toString().replaceAll(LINE_SEPARATOR, " ");
			StringBuffer oneLineB = new StringBuffer(
					"<!DOCTYPE html><html lang=\\\"en\\\"><head><meta charset=\\\"utf-8\\\"></head><body>");

			oneLineB.append(oneLIne);

			int levelCounter = 0;

			boolean skip = false;

			for (int i = oneLineB.length() - 1; i >= 0; i--) {

				char c = oneLineB.charAt(i);

				if (c == '}') {
					skip = true;
				}

				if (c == '{') {
					skip = false;
				}

				if (skip) {
					continue;
				}

				if (c == ']') {
					oneLineB.insert(i+1, LINE_SEPARATOR);
					levelCounter = 0;
				}

				if (c == '(') {

					for (int j = 0; j < levelCounter * 2; j++) {
						oneLineB.insert(i, TAB);
					}

					oneLineB.insert(i, LINE_SEPARATOR);
					levelCounter--;
				}

				if (c == ')') {

					oneLineB.insert(i + 1, LINE_SEPARATOR);

					for (int j = 0; j < levelCounter * 2; j++) {
						oneLineB.insert(i + 1 + LINE_SEPARATOR.length(), TAB);
					}

					levelCounter++;
				}

			}

			oneLineB.append("</body></html>");

			BufferedWriter bwr = new BufferedWriter(
					new FileWriter(new File(file.getAbsolutePath().replace(".pgn", ".html"))));

			// write contents of StringBuffer to a file
			bwr.write(oneLineB.toString().replace("{", "<strong> ").replace("}", " </strong>").replaceAll("\\[Event", LINE_SEPARATOR + LINE_SEPARATOR + "\\[Event"));

			// flush the stream
			bwr.flush();

			// close the stream
			bwr.close();

			System.out.println("Content of StringBuffer written to File.");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
