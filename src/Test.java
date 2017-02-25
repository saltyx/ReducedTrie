import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import Trie.Trie;

/**
 * Created by shiyan on 2017/2/16.
 * Contact: shiyan233@hotmail.com
 *          saltyx.github.io
 */
public class Test {

    public static void main(String[] argv) throws IOException {
        Files.walkFileTree(Paths.get("./test"), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                info("Visiting test folder");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().matches("^[^.,/]+.dic$")) {
                    info("Visiting "+file.getFileName());
                    Trie trie = new Trie();
                    List<String> list = new ArrayList<>();
                    Files.lines(file).distinct().forEach(list::add);
                    long start = System.currentTimeMillis();
                    trie.build(list);
                    long end = System.currentTimeMillis();
                    info("Reduced Trie has been built");
                    info(String.format("Time Used: %d ms", end-start));

                    info("=============saving trie==============");
                    trie.save("./data/test");
                    info("trie has been saved in test");

                    info("=============reload trie==============");
                    trie.load("./data/test");
                    info("trie has been loaded from test");

                    info("=============insert test==============");
                    Scanner in = new Scanner(System.in);
                    while (in.hasNext()) {
                        String str = in.next();
                        if (str.contentEquals("exit")) break;
                        trie.insert(str);
                        if (trie.search(str)) {
                            hint(str+" pass");
                        }
                    }

                    info("Running all test cases");
                    int passed = 0, failed = 0;
                    long start1 = System.currentTimeMillis();
                    for (String x: list) {
                        if (trie.search(x)) {
                            passed++;
                        } else {
                            failed++;
                            error(x+" failed");
                        }
                    }
                    long end1 = System.currentTimeMillis();
                    info("=============search test result==============");
                    if (list.size() > 1) {
                        info("$Total : "+String.valueOf(list.size()) + " entries");
                    } else {
                        info("$Total 1 entry");
                    }
                    info(String.format("$Pass %d\t$Fail %d\t$Time Used %d ms", passed, failed, end1-start1));

                    info("=============prefix test result==============");

                    Scanner in1 = new Scanner(System.in);
                    while (in1.hasNext()) {
                        String str = in1.next();
                        if (str.contentEquals("exit")) break;
                        long beginPrefixTime = System.currentTimeMillis();
                        List<String> list1 = trie.findByPrefix(str);
                        long endPrefixTime = System.currentTimeMillis();
                        if (list1 != null) {
                            list1.forEach(Test::hint);
                            hint(String.format("Total: %d, Time Used: %d ms", list1.size(), endPrefixTime-beginPrefixTime));
                        } else {
                            info("no hint");
                        }
                    }
                    info("=============delete test result==============");
                    failed = passed = 0;
                    for (String x: list) {
                        if (trie.delete(x)) {
                            if (trie.search(x)) {
                                failed++;
                            } else {
                                passed++;
                            }
                        } else {
                            failed++;
                        }
                    }
                    if (list.size() > 1) {
                        info("$Total : "+String.valueOf(list.size()) + " entries");
                    } else {
                        info("$Total 1 entry");
                    }
                    info(String.format("$Pass %d\t$Fail %d", passed, failed));
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    private static void hint(String hint) {
        System.out.println(String.format("[hint] %s", hint));
    }

    private static void info(String info) {
        System.out.println(String.format("[info] %s", info));
    }

    private static void error(String err) {
        System.err.println(String.format("[error] %s", err));
    }
}
