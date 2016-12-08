/*
Test class for generative project
*/

class GenerativeProjectTest {

    public GenerativeProjectTest() {

    }

    public void runAll() {

        // TEST 1

        println("***** TEST 1 *****\nAdd languages of two repos...");

        ArrayList<RepoLanguage> list1 = new ArrayList<RepoLanguage>();
        list1.add(new RepoLanguage("CSS", 0.2));
        list1.add(new RepoLanguage("HTML", 0.8));

        ArrayList<RepoLanguage> list2 = new ArrayList<RepoLanguage>();
        list2.add(new RepoLanguage("CSS", 0.5));
        list2.add(new RepoLanguage("HTML", 0.2));
        list2.add(new RepoLanguage("JS", 0.3));

        ArrayList<RepoLanguage> resultList = addRepoLanguages(list1, list2);
        for (int i = 0; i < resultList.size(); i++) {
            println("Language no. " + (i + 1) + " -> " + resultList.get(i).name + ", " + resultList.get(i).percentage);
        }

        // TEST 2

        println("***** TEST 2 *****\nGet random user...");
        String x = getRandomUser();
        println(x);

        // ...

        // RepoLanguage repoLanguage = new RepoLanguage();

        // Get languages from specified repo
        // repoLanguages = getRepoLanguages(testRepo);

        // Get languages from random user of GitHub
        // repoLanguages = getRandomUserLanguages();

        // testing

        println("---- END TESTS ----");

    }

}