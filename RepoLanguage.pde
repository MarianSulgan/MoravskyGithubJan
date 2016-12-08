/*
Repo Language class
*/

class RepoLanguage implements Comparable {

    String name;
    float percentage;
    int amount;

    public RepoLanguage() {

    }

    public RepoLanguage(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public RepoLanguage(String name, float percentage) {
        this.name = name;
        this.percentage = percentage;
    }

    /* Draws representation of object based on language name and percentage */

    public void draw(float x, float y, int pieceSize) {
        int a = round(this.percentage * pieceSize * 100);
        ellipse(x, y, a, a);
    }

    public String toString() {
        return "RepoLanguage: name=" + this.name + 
            " percentage=" + this.percentage * 100 + "%";
    }

    public int compareTo(Object r) {
        float result = this.percentage - ((RepoLanguage)r).percentage;
        if (result > 0)
            return 1;
        if (result < 0)
            return -1;
        else
            return 0;
    }   

}