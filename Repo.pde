/*
Repo class
*/

class Repo {

    ArrayList<RepoLanguage> languages;
    int sum;
    String user;
    String name;

    public Repo() {
        this.languages = new ArrayList<RepoLanguage>();
    }

    public Repo(ArrayList<RepoLanguage> list) {
        this.languages = list;
    }

    public Repo(String user, String name, ArrayList<RepoLanguage> list) {
        this.user = user;
        this.name = name;
        this.languages = list;
    }

    public Repo(String user, String name, ArrayList<RepoLanguage> list, int sum) {
        this.user = user;
        this.name = name;
        this.languages = list;
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "Repo -> " + this.user + " / " + this.user + " : " + languages.size() + " languages";
    }

}