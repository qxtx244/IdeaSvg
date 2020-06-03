package com.qxtx.idea.ideasvgdemo;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add("abcdefghijklmnopqrstuvwxyzsdjfkdslfjionklnvsdkflgjwiroghklsdn" + i);
        }
        long durationMs = System.currentTimeMillis();
        File file = new File("W:\\QXTX-File\\ideaSvg.xml");
        try (FileWriter fw = new FileWriter(file, true)) {
            for (int i = 0; i < list.size(); i++) {
                fw.write(list.get(i) + "\n");
            }
            fw.flush();
        }

        System.out.println("耗时：" + (System.currentTimeMillis() - durationMs) + "ms.");
    }
}