package jp.co.sis.app.ess;

import java.io.File;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import jp.co.sis.selenium.appcommon.AbstractTestEss;


/**
 * 画面打鍵テスト用メインクラス
 * @author @duc
 * @version 1.0.0
 *
 */
public class EssTestMain extends AbstractTestEss {

	/**
	 *  テスト用Excelファイル
	 */
	protected static String fileName = ".\\input\\ess_test.xlsx";

    @Before
    public void setUp() throws Exception {

    	// キャプチャ出力先
    	imageFileDir = ".\\screenshot\\ess\\";

    	// ロガー
    	logger = LoggerFactory.getLogger(EssTestMain.class);
		log = (ch.qos.logback.classic.Logger) logger;
		log.setLevel(Level.INFO);

    	// 待ち時間を3秒に設定
    	waitSeconds = 3;

		// Excelファイルを取得
    	File file = new File(fileName);
    	if (!file.exists()) {
    		logger.error("対象ファイルが存在しません。:" + fileName);
    		throw new Exception();
    	}

		workbook = WorkbookFactory.create(file);
    	// 1シート目を取得
    	Sheet sheet = workbook.getSheetAt(0);
    	// 2行目を取得
    	Row row = sheet.getRow(1);
		baseUrl = row.getCell(0).getStringCellValue();
		basePath = "/" + row.getCell(1).getStringCellValue();
		baseSoshikiCd = "/" + row.getCell(2).getStringCellValue();

        super.setUp();
    }

    @Test
    @Override
    public void testMain() throws Exception {

		// ヘッダーログ出力
		logger.info("行番号,ユーザーID,パスワード,パス,画面名,結果");

    	// 2シート目を取得
    	Sheet sheet = workbook.getSheetAt(1);
    	Iterator<Row> rows = sheet.rowIterator();
    	int cnt = 0;
    	while (rows.hasNext()) {

    		// エラーフラグ
    		boolean errorFlag  = true;

    		Row row = rows.next();
    		// 1行目はヘッダー行のため、スキップ
    		if (cnt > 0) {
    			// （A列）ユーザーID
    			String userId = row.getCell(0).getStringCellValue();
    			// （B列）パスワード
    			String password = row.getCell(1).getStringCellValue();
    			// （C列）パス
    			String path = row.getCell(2).getStringCellValue();
    			// （D列）画面名
    			String dispName = row.getCell(3).getStringCellValue();

    			// ログイン
    			super.login(userId, password);

    			try {
					// 画面打鍵
					driver.get(baseUrl + basePath + "/" + path);
					wait.until(ExpectedConditions.titleContains(dispName));
					// 結果ログ出力
	    			logger.info(cnt + "," + userId + "," + password + "," + path + "," + dispName + ",OK");

    			} catch (TimeoutException e) {
					errorFlag = false;
					// 結果ログ出力
	    			logger.error(cnt + "," + userId + "," + password + "," + path + "," + dispName + ",NG");

				}

				// 画面キャプチャ取得
				StringBuffer fname = new StringBuffer();
//				fname.append(sdf.format(Calendar.getInstance().getTime())).append("_");
				fname.append(cnt).append("_");
				fname.append(userId).append("_");
				fname.append(dispName);
				if (!errorFlag) {
					// 画面遷移失敗時は、ファイル名にERRORを付けて保存
					fname.append("_ERROR");
				}
				takeScreenShotAll(fname.toString());

				// ログアウト
				super.logout();
			}
			cnt++;
		}
	}

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
