package com.upixels.jh.hearingassist;

import org.junit.Test;

import androidx.annotation.NonNull;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.jh.BTProtocol;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        BTProtocol.ModeFileContent content0 = new BTProtocol.ModeFileContent();
        content0.aidMode = new AIDMode("name0", (byte) 1, (byte)2, (byte)3);
        content0.INPUT = 10;
        content0.EQ1 = 10;

        BTProtocol.ModeFileContent content1 = (BTProtocol.ModeFileContent) content0.clone();
        content1.aidMode.setDeviceName("name1");
        content1.aidMode.setMode((byte)2);
        content1.INPUT = 11;
        content1.EQ1 = 11;

        System.out.println("content0 = " + content0.aidMode);
        System.out.println("content1 = " + content1.aidMode);
        System.out.println("content0.aidMode = " + content0.aidMode);
        System.out.println("content1.aidMode = " + content1.aidMode);

        System.out.println(content0);
        System.out.println(content1);
    }

    public static class Mode implements Cloneable {
        String name;
        public Mode(String name) {
            this.name = name;
        }

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    @Test
    public void testClone2() throws CloneNotSupportedException {
        Mode mode0 = new Mode("name0");
        Mode mode1 = (Mode) mode0.clone();
        mode1.name = "1";

        System.out.println(mode0);
        System.out.println(mode1);

        System.out.println(System.identityHashCode(mode0.name));
//        System.out.println((mode0.name.hashCode()));
        System.out.println(System.identityHashCode(mode1.name));
        System.out.println("" + (mode0.name == mode1.name));
    }


}