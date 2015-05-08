package com.lengendsec.Demo.Lucene3_5_0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Hello world!
 *
 */
public class IndexManager 
{

	private static IndexManager indexManager;
	private static String content = "";
	
	private static String INDEX_DIR = "E:\\lucene\\luceneIndex";
	private static String DATA_DIR = "E:\\lucene\\lucenedata";
   
	private static  Analyzer analyzer = null;
	private static  Directory directory = null;
	private static  IndexWriter indexWriter = null;
	public static void main( String[] args )
    {
        File fileIndex = new File(INDEX_DIR);
        if (deleteDir(fileIndex)) {
			fileIndex.mkdir();
		} else {
			fileIndex.mkdir();
		}
        createIndex(DATA_DIR);
    	searchIndex("abc");
    }
    
    /** 
     * 创建当前文件目录的索引
     * @param path 当前文件目录
     * @return  是否成功
     */ 
    private static boolean createIndex(String path) {
    	Date dateBegin = new Date();
    	List<File> fileList = getFileList(path);
    	for (File file : fileList) {
			content = "";
			//获取文件后缀
			String type = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			if ("txt".equalsIgnoreCase(type)) {
				content += txt2String(file);
			} else if ("xls".equalsIgnoreCase(type)) {
				content += xls2String(file);
//			} else if ("doc".equalsIgnoreCase(type)) {
//				content += doc2String(file);
			}
			System.out.println("name :"+file.getName());
			System.out.println("path :"+file.getPath());
	        System.out.println("content :"+content); 
	        System.out.println();
	        try {
	        	analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
	        	File indexFile = new File(INDEX_DIR);
				directory = FSDirectory.open(indexFile);
				if (!indexFile.exists()) {
					indexFile.mkdirs();
				}
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
				indexWriter = new IndexWriter(directory, config);
				Document document = new Document();
				document.add(new Field("filename", file.getName(), Store.YES,  Field.Index.NOT_ANALYZED));
				document.add(new Field("content", content, Store.YES, Field.Index.ANALYZED));
				document.add(new Field("path", file.getPath(), Store.YES, Field.Index.NOT_ANALYZED));
				indexWriter.addDocument(document);
				closeWriter();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        content = "";
		}
    	Date dateEnd = new Date();
    	System.out.println("创建索引-----耗时：" + (dateEnd.getTime() - dateBegin.getTime()) + "ms\n");
    	return true;
    }
    private static void closeWriter() throws CorruptIndexException, IOException {
    	if (indexWriter != null) {
    		indexWriter.close();
    	}
	}

	/**     
     * 读取txt文件的内容
     * @param file 想要读取的文件对象
     * @return  返回文件内容
     */
	private static String txt2String(File file) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			//构造一个BufferedReader类来读取文件
			String str = null;
			while((str = br.readLine()) != null) {
				//使用readLine方法，一次读一行
				result += str + "\n";
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	private static String xls2String(File file) {
		String result = "";
		try {
			FileInputStream fis = new FileInputStream(file);   
			StringBuilder sb = new StringBuilder();   
			jxl.Workbook rwb = Workbook.getWorkbook(fis);
			Sheet[] sheets = rwb.getSheets();
			for (Sheet sheet : sheets) {
				for (int i = 0; i < sheet.getRows(); i++) {
					Cell[] cells = sheet.getRow(i);
					for (Cell cell : cells) {
						sb.append(cell.getContents());
					}
				}
			}
			fis.close();
			result += sb.toString();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取doc文件内容
	 * @param file 想要读取的文件对象
	 * @return  返回文件内容
	 */
	/*private static String doc2String(File file) {
		String result = "";
		try {
			FileInputStream fis = new FileInputStream(file);
			HWPFDocument doc = new HWPFDocument(fis);
			Range range = doc.getRange();
			result += rang.text();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}*/

	private static List<File> getFileList(String dirPath) {
		File[] files = new File(dirPath).listFiles();
		List<File> fileList = new ArrayList<File>();
		for (File file : files) {
			if (isTxtFile(file.getName())) {
				fileList.add(file);
			}
		}
		return fileList;
	}
	/**     
	 * 判断是否为目标文件，目前支持txt xls doc格式
	 * @param fileName 文件名称
	 * @return  如果是文件类型满足过滤条件，返回true；否则返回false
	 */
	private static boolean isTxtFile(String fileName) {
		if (fileName.lastIndexOf(".txt") > 0) {
			return true;
		} else if (fileName.lastIndexOf(".xls") > 0) {
			return true;
		} else if (fileName.lastIndexOf(".doc") > 0) {
			return true;
		}
		return false;
	}

	private static boolean searchIndex(String text) {
		Date dateBegin = new Date();
		try {
			IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(INDEX_DIR)));
	        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	        analyzer = new StandardAnalyzer(Version.LUCENE_35);
	        QueryParser parser = new QueryParser(Version.LUCENE_35, "content", analyzer);
	        Query query = parser.parse(text);
	        TopDocs result = indexSearcher.search(query, 10000);
	        ScoreDoc[] hits = result.scoreDocs;
	        for (int i = 0; i < hits.length; i++) {
	        	Document hitDoc = indexSearcher.doc(hits[i].doc);
	        	System.out.println("-----------------------");
				System.out.println(hitDoc.get("filename"));
				System.out.println(hitDoc.get("path"));
				System.out.println(hitDoc.get("content"));
				System.out.println("-----------------------");
			}
	        indexSearcher.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date dateEnd = new Date();
		System.out.println("查看索引-----耗时：" + (dateEnd.getTime() - dateBegin.getTime()) + "ms\n");
		return false;
	}
	private static boolean deleteDir(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteDir(files[i]);
			}
		}
		file.delete();
		return true;
	}
}
