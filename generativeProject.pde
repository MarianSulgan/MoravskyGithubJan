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

// Get access token from github account 
// WARNING: otherwise, it is limited to 60 requests per hour!
String access_token = "";

ArrayList<RepoLanguage> repoLanguages = new ArrayList<RepoLanguage>();
ArrayList<Element> elements = new ArrayList<Element>();

// Size of grid
int gridSizeX = 16;
int gridSizeY = 9;

int stepX, stepY;
int elementSizeX, elementSizeY;
int state = 0;
int drawIndex = 0;

// Number of repos to be continually analyzed
int stopCount = 50;

ArrayList<PShape> svgs = new ArrayList<PShape>();

void setup() {

  size(1920, 1080);
  background(#d6cbc7);
  noStroke();
  colorMode(HSB, 360, 100, 100);

  // Run tets
  if (TESTS) {
    GenerativeProjectTest tests = new GenerativeProjectTest();
    tests.runAll();
  }

  stepX = width / gridSizeX;
  stepY = height / gridSizeY;

  // Init svgs
  for (int i = 0; i < 14; i++) {
    PShape ps = loadShape("svg/folk" + (i+1) + ".svg");
    svgs.add(ps);
  }

}

void runGitMagic() {
  repoLanguages = new ArrayList<RepoLanguage>();
  elements = new ArrayList<Element>();
  try {

    // Get random  usr
    repoLanguages = getRandomUserLanguages();

    // ...or get specified user
    // repoLanguages = getUserLanguages("Firelizzard-Inventions");

    for (int i = 0; i < repoLanguages.size(); i++) {
        println("Language no. " + (i + 1) + " -> " + repoLanguages.get(i).name + ", " + repoLanguages.get(i).percentage * 100.0 + "%");
    }

  } catch (Exception e) {
    println("Exception occured: " + e);
  }
}

void draw() {

  if (state == 0) {

    runGitMagic();

    // Validate repository languages

    if (repoLanguages != null && repoLanguages.size() != 0) {

      // Draw in grid

      int counter = 0;
      int langCounter = 0;
      int gridCount = (width/stepX) * (height/stepY);
      String elem = "";
      int elemCounter = int(random(0, svgs.size()));

      for (int i = stepY; i <= height; i += stepY) {
        for (int j = stepX; j <= width; j += stepX) {
          
          float x = j - 0.5 * stepX;
          float y = i - 0.5 * stepY;

          // println("Visualizing language no. " + langCounter);
          // println(repoLanguages);

          if (langCounter >= repoLanguages.size()) break;

          elem = repoLanguages.get(langCounter).name;

          float d = round(repoLanguages.get(langCounter).percentage * 100.0) / 100.0;
          while (d == 0.0) {
            if (langCounter < repoLanguages.size()) langCounter++;
            if (langCounter == repoLanguages.size()) break;
            d = round(repoLanguages.get(langCounter).percentage * 100.0) / 100.0;
          }

          if (float(counter) < ((d * gridCount) - 1)) {
            counter++;
          } else {
            langCounter++;
            counter = 0;
          }

          elements.add(new Element(x - stepX/2, y - stepY/2, stepX * 1.0, stepX * 1.0, elem, svgs));
          
        }
      }
      state = 1;
    } else {

      // Empty stuff, maybe display notification?

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

    // Stop here...
    noLoop();

  }
}

String getRandomUser() {

  // One of 3 millions of users (3000000 is made up, change for any reasonable number)
  float _rand = random(0, 3000000);
  int rand = int(_rand);
  
  String request = "https://api.github.com/users?since=" + rand + "&per_page=1&" + access_token;
  String result = join(loadStrings(request),"");
  result = result.replace("[","");
  result = result.replace("]","");

  // Repos data
  JSONObject data = JSONObject.parse(result);

  return data.getString("login");
}

ArrayList<Repo> getUserRepos(String repoUser) {

  ArrayList<Repo> resultList = new ArrayList<Repo>();

  String request = "https://api.github.com/users/" + repoUser + "/repos?" + access_token;
  JSONArray arr = new JSONArray();

  try {

    arr = loadJSONArray(request);

  } catch (Exception e) {

    println("Unable to load JSON array from URL: " + request);

  }

  // Get all repos analyzed
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

  return resultList;
}

ArrayList<RepoLanguage> getRandomUserLanguages() {

  String user = getRandomUser();

  return getUserLanguages(user);
}

ArrayList<RepoLanguage> getUserLanguages(String repoUser) {

  String x = repoUser;

  // Get user repos
  ArrayList<Repo> userRepos = getUserRepos(repoUser);

  // If there is no repo or repo is empty, get a random new one!
  while (userRepos.size() == 0 || (userRepos.size() == 1 && userRepos.get(0).languages.size() == 0)) {
    x = getRandomUser();
    userRepos = getUserRepos(x);
  }

  println("User: " + x);

  int reposSum = 0;

  // Get languages summary
  if (userRepos.size() > 0) {

    ArrayList<RepoLanguage> firstRepoLanguages = userRepos.get(0).languages;
    ArrayList<RepoLanguage> resultLanguages = firstRepoLanguages;

    for (int i = 1; i < userRepos.size(); i++) {
      resultLanguages = addRepoLanguages(resultLanguages, userRepos.get(i).languages);
    }

    // Total sum of language usage
    for (int i = 0; i < userRepos.size(); i++) {
      reposSum += userRepos.get(i).sum;
    }

    if (reposSum == 0) {
      println("No language info detected, displaying empty container.");
      return null;
    }

    for (int i = 0; i < resultLanguages.size(); i++) {
      resultLanguages.get(i).percentage = float(resultLanguages.get(i).amount) / float(reposSum);
    }

    return resultLanguages;

  } else {

    println("Empty repository. Nothing to show :-(");

    return null;

  }
}

Repo getRepoLanguages(String repoUser, String repoName) {

  return getRepoLanguages(repoUser + "/" + repoName);

};

Repo getRepoLanguages(String repoUserAndName) {

  ArrayList<RepoLanguage> resultList = new ArrayList<RepoLanguage>();

  // Get JSON data on repository languages
  String request = baseURL + repoUserAndName + langSuffix + "?" + access_token;
  String result = join(loadStrings(request),"");

  JSONObject repoLanguageData = JSONObject.parse(result);

  String keys[] = (String[]) repoLanguageData.keys()
    .toArray(new String[repoLanguageData.size()]);

  // Parse language info from JSON
  int sum = 0;
  for (String key : keys) {
    sum += repoLanguageData.getInt(key);
  }

  Repo repo = new Repo();
  repo.sum = sum;

  for (String key : keys) {
    String languageName = key;
    repo.languages.add(new RepoLanguage(languageName, repoLanguageData.getInt(key)));
  }

  java.util.Collections.sort(repo.languages);

  return repo;

}

ArrayList<RepoLanguage> addRepoLanguages(ArrayList<RepoLanguage> listA, ArrayList<RepoLanguage> listB) {

  boolean found = false;
  
  ArrayList<RepoLanguage> resultList = listA;

  for (int i = 0; i < listB.size(); i++) {
    for (int j = 0; j < listA.size(); j++) {

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

  return resultList;
} 

void restart() {
  elements.clear();
  repoLanguages.clear();
  drawIndex = 0;
  background(#d6cbc7);
  state = 0;
}

void keyPressed() {

  if (key == 's') saveFrame("gen-####.png");

  if (key == 'r') {
    restart();
  }
}
