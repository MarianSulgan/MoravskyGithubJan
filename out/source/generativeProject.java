import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class generativeProject extends PApplet {

/*

Final project for course Programming generative design
Marian Sulgan, 374456
@FI.MUNI

Description:
Visualization of github repositories

*/

boolean TESTS = false;

String baseURL = "https://api.github.com/repos/";
String langSuffix = "/languages";

// get access token from github account 
// WARNING: otherwise, it is limited to 60 requests per hour!
String access_token = "";

ArrayList<RepoLanguage> repoLanguages = new ArrayList<RepoLanguage>();
ArrayList<Element> elements = new ArrayList<Element>();

// size of one piece in pixels
int pieceSize = 5;

// size of grid
int gridSizeX = 16;
int gridSizeY = 9;

int stepX, stepY;
int elementSizeX, elementSizeY;

float resizeCoef = 0.9f;
int state = 0;
int drawIndex = 0;

// number of repos to be continually analyzed
int stopCount = 50;

ArrayList<PShape> svgs = new ArrayList<PShape>();

public void setup() {

  
  background(0xffd6cbc7);
  noStroke();
  colorMode(HSB, 360, 100, 100);

  if (TESTS) {
    GenerativeProjectTest tests = new GenerativeProjectTest();
    tests.runAll();
  }

  stepX = width / gridSizeX;
  stepY = height / gridSizeY;

  // init svgs
  for (int i = 0; i < 14; i++) {
    PShape ps = loadShape("svg/folk" + (i+1) + ".svg");
    svgs.add(ps);
  }

}

public void runGitMagic() {
  repoLanguages = new ArrayList<RepoLanguage>();
  elements = new ArrayList<Element>();
  try {

    // get random 
    repoLanguages = getRandomUserLanguages();

    // or get specified user
    // repoLanguages = getUserLanguages("Firelizzard-Inventions");
    for (int i = 0; i < repoLanguages.size(); i++) {
        println("Language no. " + (i + 1) + " -> " + repoLanguages.get(i).name + ", " + repoLanguages.get(i).percentage * 100.0f + "%");
    }
  } catch (Exception e) {
    println("Exception occured: " + e);
  }
}

public void draw() {

  if (state == 0) {

    runGitMagic();

    // Validate repository languages

    if (repoLanguages != null && repoLanguages.size() != 0) {

      // Draw in grid

      int counter = 0;
      int langCounter = 0;
      int gridCount = (width/stepX) * (height/stepY);
      String elem = "";
      int elemCounter = PApplet.parseInt(random(0, svgs.size()));

      for (int i = stepY; i <= height; i += stepY) {
        for (int j = stepX; j <= width; j += stepX) {
          
          float x = j - 0.5f * stepX;
          float y = i - 0.5f * stepY;
          // float size = resizeCoef * stepX;

          // println("Visualizing language no. " + langCounter);
          // println(repoLanguages);

          if (langCounter >= repoLanguages.size()) break;

          elem = repoLanguages.get(langCounter).name;

          float d = round(repoLanguages.get(langCounter).percentage * 100.0f) / 100.0f;
          while (d == 0.0f) {
            if (langCounter < repoLanguages.size()) langCounter++;
            if (langCounter == repoLanguages.size()) break;
            d = round(repoLanguages.get(langCounter).percentage * 100.0f) / 100.0f;
          }

          if (PApplet.parseFloat(counter) < ((d * gridCount) - 1)) {
            counter++;
          } else {
            langCounter++;
            counter = 0;
          }

          elements.add(new Element(x - stepX/2, y - stepY/2, stepX * 1.0f, stepX * 1.0f, elem, svgs));
          
        }
      }
      state = 1;
    } else {

      // empty stuff, maybe display notification?

    }

  } else if (state == 1) {

    // Rendering too fast, slow down to every third frame
    if (frameCount % 3 == 0) {

      // Save every new frame?
      // saveFrame("screens/gen-####.png");

      if (drawIndex < elements.size()) {
        elements.get(drawIndex++).draw();
      } else {
        drawIndex = 0;
        state = 2;
      }
    }

  } else if (state == 2) {

    // Wait for two seconds and make the magic happen again...

    // Save final frame for repo?
    // saveFrame("screen-###.png");

    delay(2000);
    restart();

    if (--stopCount == 0)
      state = 3;

  } else if (state == 3) {

    noLoop();

  }
  
  // noLoop();
  // stop();

}

public String getRandomUser() {

  // one of 3 millions of users
  float _rand = random(0, 3000000);
  int rand = PApplet.parseInt(_rand);
  
  String request = "https://api.github.com/users?since=" + rand + "&per_page=1&" + access_token;
  String result = join(loadStrings(request),"");
  result = result.replace("[","");
  result = result.replace("]","");

  // repos data
  JSONObject data = JSONObject.parse(result);

  // println(data.getString("login"));

  return data.getString("login");
}

public ArrayList<Repo> getUserRepos(String repoUser) {

  // println("Called getUserRepos(String repoUser) with param " + repoUser);

  ArrayList<Repo> resultList = new ArrayList<Repo>();

  String request = "https://api.github.com/users/" + repoUser + "/repos?" + access_token;
  JSONArray arr = new JSONArray();

  try {

    arr = loadJSONArray(request);

  } catch (Exception e) {

    println("Unable to load JSON array from URL: " + request);

  }

  // get all repos analyzed
  for (int i = 0; i < arr.size(); i++) {
    
    String repoName = arr.getJSONObject(i).getString("name");

    // println("Analyzing repo no. " + (i + 1) + " -> " + repoName + " by " + repoUser);

    Repo tmpRepo = getRepoLanguages(repoUser, repoName); 

    Repo repo = new Repo(
      repoUser,
      repoName,
      tmpRepo.languages,
      tmpRepo.sum
    );
    resultList.add(repo);
  }

  // println("Returning result list with Repos");
  
  return resultList;
}

public ArrayList<RepoLanguage> getRandomUserLanguages() {

  // get random user
  String user = getRandomUser();

  return getUserLanguages(user);
}

public ArrayList<RepoLanguage> getUserLanguages(String repoUser) {

  String x = repoUser;

  // get user repos
  ArrayList<Repo> userRepos = getUserRepos(repoUser);

  // if there is no repo or repo is empty, get a random new one!
  while (userRepos.size() == 0 || (userRepos.size() == 1 && userRepos.get(0).languages.size() == 0)) {
    x = getRandomUser();
    userRepos = getUserRepos(x);
  }

  println("User: " + x);

  int reposSum = 0;

  // get languages summary
  if (userRepos.size() > 0) {

    // ArrayList<RepoLanguage> resultLanguages = new ArrayList<RepoLanguage>();
    ArrayList<RepoLanguage> firstRepoLanguages = userRepos.get(0).languages;
    // println(firstRepoLanguages);
    ArrayList<RepoLanguage> resultLanguages = firstRepoLanguages;

    for (int i = 1; i < userRepos.size(); i++) {
      // println(userRepos.get(i).languages);
      resultLanguages = addRepoLanguages(resultLanguages, userRepos.get(i).languages);
      // langCounter += userRepos.get(i).languages.size();
      // reposSum += userRepos.get(i).sum;
    }

    // Total sum of language usage
    for (int i = 0; i < userRepos.size(); i++) {
      reposSum += userRepos.get(i).sum;
    }

    // println("Repo sum: " + reposSum);

    if (reposSum == 0) {
      // display something, like there is no language info
      println("No language info detected, displaying empty container.");
      return null;
    }

    // correction
    // int validRepoLanguageCounter = 0;
    // for (int i = 1; i < userRepos.size(); i++) {
    //   if (userRepos.get(i).languages.size() != 0)
    //     validRepoLanguageCounter++;
    // }
    for (int i = 0; i < resultLanguages.size(); i++) {
      // println("Amount: " + resultLanguages.get(i).amount);
      resultLanguages.get(i).percentage = PApplet.parseFloat(resultLanguages.get(i).amount) / PApplet.parseFloat(reposSum);
    }

    return resultLanguages;

  } else {

    println("Empty repository. Nothing to show :-(");
    
    // do action in processing, show empty space or something...
    ellipse(height/2, width/2, width * resizeCoef, height * resizeCoef);

    return null;

  }
}

public Repo getRepoLanguages(String repoUser, String repoName) {

  return getRepoLanguages(repoUser + "/" + repoName);

};

public Repo getRepoLanguages(String repoUserAndName) {

  ArrayList<RepoLanguage> resultList = new ArrayList<RepoLanguage>();

  // get JSON data on repository languages

  String request = baseURL + repoUserAndName + langSuffix + "?" + access_token;
  String result = join(loadStrings(request),"");

  JSONObject repoLanguageData = JSONObject.parse(result);

  // println(repoLanguageData);

  String keys[] = (String[]) repoLanguageData.keys()
    .toArray(new String[repoLanguageData.size()]);

  // println(keys);
  
  // parse language info from JSON

  int sum = 0;

  for (String key : keys) {
    sum += repoLanguageData.getInt(key);
  }

  Repo repo = new Repo();
  repo.sum = sum;

  for (String key : keys) {
    // float languagePercentage = repoLanguageData.getInt(key) / (sum * 1.0);
    String languageName = key;
    repo.languages.add(new RepoLanguage(languageName, repoLanguageData.getInt(key)));
  }

  java.util.Collections.sort(repo.languages);

  // println(resultList);

  return repo;

}

public ArrayList<RepoLanguage> addRepoLanguages(ArrayList<RepoLanguage> listA, ArrayList<RepoLanguage> listB) {

  boolean found = false;
  
  ArrayList<RepoLanguage> resultList = listA;

  for (int i = 0; i < listB.size(); i++) {
    for (int j = 0; j < listA.size(); j++) {

      // println(listA.get(j).name, listB.get(i).name);

      if (listA.get(j).name == listB.get(i).name) {
        resultList.get(j).amount = listA.get(j).amount + listB.get(i).amount;
        found = true;
        break;
      } else {
        found = false;
      }
    }

    if (!found) {
      resultList.add(listB.get(i));
    }

  }

  // correction of sum
  // for (RepoLanguage rl : resultList) {
  //   rl.percentage /= 2;
  // }

  return resultList;
} 

public void restart() {
  elements.clear();
  repoLanguages.clear();
  drawIndex = 0;
  background(0xffd6cbc7);
  state = 0;
}

public void keyPressed() {

  if (key == 's') saveFrame("gen-####.png");

  if (key == 'r') {
    restart();
  }

}
/*
Color HSB
*/

class ColorHSB {

    int hue;
    int saturation;
    int brightness;

    public ColorHSB() {

    }

    public ColorHSB(int hue, int saturation, int brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    @Override
    public String toString() {
        return "ColorHSB { " +hue + ", " + saturation + ", " + brightness + " }";
    }

}
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

    public void draw() {
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
            case "ObjectiveC":
                return this.ps.get(12);
            case "Scala":
                return this.ps.get(13);

            default :
                return this.ps.get(0);
        }

    }

}
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
        list1.add(new RepoLanguage("CSS", 0.2f));
        list1.add(new RepoLanguage("HTML", 0.8f));

        ArrayList<RepoLanguage> list2 = new ArrayList<RepoLanguage>();
        list2.add(new RepoLanguage("CSS", 0.5f));
        list2.add(new RepoLanguage("HTML", 0.2f));
        list2.add(new RepoLanguage("JS", 0.3f));

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
  public void settings() {  size(1920, 1080); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "generativeProject" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
