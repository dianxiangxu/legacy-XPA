package org.sag.coverage;

import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class TestSuite {
    private static Log logger = LogFactory.getLog(TestSuite.class);
    private List<String> requests;
    private List<String> oracles;

    private TestSuite(List<String> requests, List<String> oracles) {
        this.requests = requests;
        this.oracles = oracles;
    }

    public static TestSuite loadTestSuite(File csvFile) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> myEntries = reader.readAll();
            List<String> requests = new ArrayList<>();
            List<String> oracles = new ArrayList<>();
            for (String[] entry : myEntries) {
                File requestFile = new File(csvFile.getParent() + File.separator + entry[0]);
                requests.add(FileUtils.readFileToString(requestFile, StandardCharsets.UTF_8));
                oracles.add(entry[1]);
            }
            return new TestSuite(requests, oracles);
        } catch (FileNotFoundException e) {
            logger.error(e);
            return null;
        } catch (IOException e) {
            logger.error(e);
            return null;
        }
    }

    public List<Boolean> runTests(AbstractPolicy policy) {
        List<Boolean> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++)
            results.add(PolicyRunner.runTest(policy, requests.get(i), oracles.get(i)));
        return results;
    }
}
