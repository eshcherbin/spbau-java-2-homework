package ru.spbau.eshcherbin.nucleus.vcs;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VCSTreeTest {
    @Test
    public void getContentTest() throws Exception {
        VCSTree tree = new VCSTree("");
        VCSBlob blob1 = new VCSBlob("bTestContent".getBytes(), "bTest");
        VCSBlob blob2 = new VCSBlob("aTestContent".getBytes(), "aTest");
        tree.addChild(blob1);
        tree.addChild(blob2);
        String expectedTreeContent = blob2.getType().toString() + '\t' + blob2.getSha() + '\t' + blob2.getName() +
                '\n' + blob1.getType().toString() + '\t' + blob1.getSha() + '\t' + blob1.getName();
        assertThat(tree.getContent(), is(expectedTreeContent.getBytes()));
    }
}