class Element {

    float x;
    float y;
    float width;
    float height;
    String c;
    ArrayList<PShape> ps;

    public Element() {

    }

    public Element(float x, float y, float width, float height, String c) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.c = c;
    }

    public Element(float x, float y, float width, float height, String c, ArrayList<PShape> ps) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.c = c;
        this.ps = ps;
    }

    void draw() {
        shape(languageToShape(this.c), x, y, width, height);
    }

    private PShape languageToShape(String languageName) {
        switch (languageName) {
            case "JavaScript":
                return this.ps.get(1);
            case "Java":
                return this.ps.get(2);
            case "Python":
                return this.ps.get(3);
            case "Ruby":
                return this.ps.get(4);
            case "PHP":
                return this.ps.get(5);
            case "C++":
                return this.ps.get(6);
            case "CSS":
                return this.ps.get(7);
            case "C#":
                return this.ps.get(8);
            case "C":
                return this.ps.get(9);
            case "Go":
                return this.ps.get(10);
            case "Shell":
                return this.ps.get(11);
            case "Objective-C":
                return this.ps.get(12);
            case "Scala":
                return this.ps.get(13);

            default :
                return this.ps.get(0);
        }

    }

}