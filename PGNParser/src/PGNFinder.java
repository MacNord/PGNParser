
import java.io.File;
import java.util.stream.Stream;

/**
 */
public class PGNFinder {

	private static String folder = "rsc";

	public static void main(String[] args) throws Exception {

		PGNFinder validate = new PGNFinder();

		validate.findAllPGNFiles(folder);
	}

	public void findAllPGNFiles(String folder) throws Exception {

		File dir = new File(folder);
		
		PGNParserColumn pgnParserColumn = new PGNParserColumn();

		File[] pgnFiles = dir.listFiles();

		Stream.of(pgnFiles).filter(e -> e.getName().endsWith("test.pgn"))
				.forEachOrdered(w -> pgnParserColumn.modify(w));

	}

}
