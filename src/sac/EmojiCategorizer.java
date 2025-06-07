package sac;

import com.vdurmont.emoji.Emoji;

import java.util.*;

public class EmojiCategorizer {
    public Map<String, List<String>> emojiCategories = new HashMap<>();

    List<String> Category = Arrays.asList(
        "SMILEYS", "PEOPLE", "CLOTHES", "ANIMALS", "NATURE", "FOOD", "PLACES", "ACTIVITIES", "HEARTS", "FLAGS", "SYMBOLS"
    );

    public EmojiCategorizer() {
        for (String category : Category) {
            emojiCategories.put(category, new ArrayList<>());
        }
    }

    public void categorizeEmoji(Emoji emoji, String description, List<String> tags) {
        // D'abord vérifier les catégories très spécifiques
        if (tags.contains("flag") || description.contains("regional indicator") ||
                description.contains("flag:")) {
            addToCategory(emoji.getUnicode(), "FLAGS");
            return;
        }

        if (description.contains("heart") || description.contains("love") ||
                description.contains("kiss") || description.contains("cupid")) {
            addToCategory(emoji.getUnicode(), "HEARTS");
            return;
        }

        // Nouvelle catégorie pour NATURE
        if (description.contains("flower") || description.contains("blossom") ||
                description.contains("tree") || description.contains("plant") ||
                description.contains("leaf") || description.contains("petal") ||
                description.contains("tulip") || description.contains("rose") ||
                description.contains("hibiscus") || description.contains("cherry blossom") ||
                description.contains("bouquet") || description.contains("herb") ||
                description.contains("seedling") || description.contains("cactus") ||
                description.contains("mushroom") || description.contains("fallen leaf") ||
                description.contains("fluttering") || tags.contains("nature") ||
                description.contains("shamrock") || description.contains("four leaf") ||
                description.contains("forest") || description.contains("garden") ||
                description.contains("pine") || description.contains("evergreen") ||
                description.contains("palm") || description.contains("herb") ||
                description.contains("autumn") || description.contains("spring") ||
                tags.contains("plant") || tags.contains("flower") ||
                description.contains("sun") || description.contains("moon") ||
                description.contains("star") || description.contains("cloud") ||
                description.contains("rain") || description.contains("snow") ||
                description.contains("weather") || description.contains("tornado") ||
                description.contains("fog") || description.contains("cyclone") ||
                description.contains("rainbow") || description.contains("comet") ||
                description.contains("dew") || description.contains("droplet") ||
                description.contains("water") && !description.contains("potable water")) {
            addToCategory(emoji.getUnicode(), "NATURE");
            return;
        }

        // Catégorie pour les vêtements
        if (description.contains("clothing") || description.contains("dress") ||
                description.contains("shirt") || description.contains("pants") ||
                description.contains("shoes") || description.contains("boot") ||
                description.contains("coat") || description.contains("sock") ||
                description.contains("hat") || description.contains("cap") ||
                description.contains("glove") || description.contains("scarf") ||
                description.contains("tie") || description.contains("jeans") ||
                description.contains("bikini") || description.contains("kimono") ||
                description.contains("sari") || description.contains("uniform") ||
                description.contains("crown") || description.contains("handbag") ||
                description.contains("purse") || description.contains("backpack") ||
                description.contains("shoe") || description.contains("high-heeled") ||
                description.contains("sandal") || description.contains("glasses") ||
                description.contains("sunglasses") || description.contains("necktie") ||
                description.contains("t-shirt") || description.contains("jeans") ||
                tags.contains("clothing") || tags.contains("accessory")) {
            addToCategory(emoji.getUnicode(), "CLOTHES");
            return;
        }

        // Gestion des animaux avec une définition plus précise
        if (description.contains("animal") ||
                description.contains("lion") || description.contains("fox") ||
                description.contains("wolf") || description.contains("dog") ||
                description.contains("cat") || description.contains("bird") ||
                description.contains("fish") || tags.contains("pet") ||
                description.contains("monkey") || description.contains("tiger") ||
                description.contains("bear") || description.contains("rabbit") ||
                description.contains("pig") || description.contains("cow") ||
                description.contains("frog") || description.contains("horse") ||
                description.contains("mouse") || description.contains("dolphin") ||
                description.contains("whale") || description.contains("dragon") ||
                description.contains("unicorn") || description.contains("zebra") ||
                description.contains("giraffe") || description.contains("hedgehog") ||
                description.contains("dinosaur") || description.contains("rhino") ||
                description.contains("hippo") || description.contains("kangaroo") ||
                description.contains("badger") || description.contains("swan") ||
                description.contains("peacock") || description.contains("parrot") ||
                description.contains("bat") || description.contains("eagle") ||
                description.contains("duck") || description.contains("owl") ||
                description.contains("lizard") || description.contains("shark") ||
                description.contains("crab") || description.contains("shrimp") ||
                description.contains("squid") || description.contains("butterfly") ||
                description.contains("bug") || description.contains("spider") ||
                description.contains("scorpion") || description.contains("mosquito") ||
                description.contains("microbe") || description.contains("turtle") ||
                description.contains("snail") || description.contains("beetle") ||
                description.contains("ant") || description.contains("bee") ||
                description.contains("ladybug") || description.contains("cricket") ||
                description.contains("octopus") || description.contains("chipmunk") ||
                description.contains("insect") || description.contains("paw") ||
                description.contains("zoo") || description.contains("pet") ||
                description.contains("koala") || description.contains("boar") ||
                description.contains("sheep") || description.contains("penguin") ||
                description.contains("chicken") || description.contains("snake") ||
                description.contains("shell") || description.contains("ram") ||
                description.contains("rat") || description.contains("buffalo") ||
                description.contains("goat") || description.contains("rooster") ||
                description.contains("ox") || description.contains("crocodile") ||
                description.contains("camel") || description.contains("dromedary") ||
                description.contains("leopard") || description.contains("poodle")) {
            addToCategory(emoji.getUnicode(), "ANIMALS");
            return;
        }

        // Smileys (visages humains) - utilisation du return pour éviter classification multiple
        if ((description.contains("face") &&
                !description.contains("cat face") &&
                !description.contains("dog face") &&
                !description.contains("lion face") &&
                !description.contains("fox face") &&
                !description.contains("wolf face") &&
                !description.contains("monkey face") &&
                !description.contains("bear face")) ||
                description.contains("smil") || description.contains("grin") ||
                description.contains("laugh") || description.contains("cry") ||
                description.contains("frown") || description.contains("wink") ||
                description.contains("emoji") || tags.contains("smiley")) {
            addToCategory(emoji.getUnicode(), "SMILEYS");
            return;
        }

        if (description.contains("man") || description.contains("woman") ||
                description.contains("person") || description.contains("child") ||
                description.contains("family") || tags.contains("couple") ||
                description.contains("hand") || description.contains("foot") ||
                description.contains("body") || description.contains("hair") ||
                description.contains("eye") || description.contains("ear") ||
                description.contains("nose") || description.contains("mouth") ||
                description.contains("bone") || description.contains("tooth") ||
                description.contains("brain") || description.contains("tongue") ||
                description.contains("biceps") || description.contains("prince") ||
                description.contains("princess") || description.contains("wizard") ||
                description.contains("fairy") || description.contains("mermaid") ||
                description.contains("genie") || description.contains("elf") ||
                description.contains("zombie") || description.contains("vampire") ||
                description.contains("baby") || description.contains("adult") ||
                description.contains("old") || description.contains("beard") ||
                description.contains("human") || description.contains("finger") ||
                description.contains("japanese") || description.contains("skull") ||
                description.contains("alien") || description.contains("gesture") ||
                description.contains("police officer") || description.contains("construction worker") ||
                description.contains("boy") || description.contains("girl") ||
                description.contains("thumbs up") || description.contains("thumbs down") ||
                description.contains("pedestrian") || description.contains("nail polish") ||
                description.contains("bride") || description.contains("bride with veil") ||
                description.contains("silhouette") || description.contains("busts") ||
                description.contains("imp") || description.contains("ghost") ||
                description.contains("father christmas") || tags.contains("user") ||
                tags.contains("users") || tags.contains("group") || tags.contains("team") ||
                description.contains("devil") || description.contains("sleeping") ||
                description.contains("santa") || description.contains("christmas") ||
                description.contains("saint") || description.contains("angel") ||
                description.contains("superhero") || description.contains("supervillain") ||
                description.contains("mother christmas") || description.contains("shrug") ||
                description.contains("fist") || description.contains("wrestlers") ||
                description.contains("fencer") || description.contains("juggling") ||
                description.contains("sleeping") || description.contains("breastfeeding") ||
                description.contains("mage") || description.contains("palms")) {
            addToCategory(emoji.getUnicode(), "PEOPLE");
            return;
        }

        if (description.contains("food") || description.contains("fruit") ||
                description.contains("vegetable") || description.contains("meal") ||
                description.contains("drink") || description.contains("restaurant") ||
                description.contains("apple") || description.contains("banana") ||
                description.contains("orange") || description.contains("strawberry") ||
                description.contains("bread") || description.contains("cheese") ||
                description.contains("meat") || description.contains("egg") ||
                description.contains("ice cream") || description.contains("cake") ||
                description.contains("candy") || description.contains("coffee") ||
                description.contains("tea") || description.contains("beer") ||
                description.contains("wine") || description.contains("cocktail") ||
                description.contains("pizza") || description.contains("hamburger") ||
                description.contains("sushi") || description.contains("taco") ||
                description.contains("burrito") || description.contains("popcorn") ||
                description.contains("cooking") || description.contains("kitchen") ||
                description.contains("chef") || description.contains("breakfast") ||
                description.contains("lunch") || description.contains("dinner") ||
                description.contains("dessert") || description.contains("snack") ||
                tags.contains("food") || tags.contains("drink") ||
                description.contains("sake") || description.contains("fork and knife") ||
                description.contains("french fries") || description.contains("poultry") ||
                description.contains("spaghetti") || description.contains("curry") ||
                description.contains("bento") || description.contains("rice") ||
                description.contains("oden") || description.contains("dango") ||
                description.contains("doughnut") || description.contains("shaved ice") ||
                description.contains("cookie") || description.contains("chocolate") ||
                description.contains("lollipop") || description.contains("honey") ||
                description.contains("tangerine") || description.contains("lemon") ||
                description.contains("cherries") || description.contains("grapes") ||
                description.contains("watermelon") || description.contains("peach") ||
                description.contains("melon") || description.contains("sweet potato") ||
                description.contains("aubergine") || description.contains("tomato") ||
                description.contains("hot pepper") || description.contains("bottle") ||
                description.contains("popping cork") || description.contains("cutlery") ||
                description.contains("spoon") || description.contains("plate") ||
                description.contains("sandwich") || description.contains("dumpling") ||
                description.contains("fortune cookie") || description.contains("takeout") ||
                description.contains("pie") || description.contains("cup with straw") ||
                description.contains("chopsticks") || description.contains("potato") ||
                description.contains("carrot") || description.contains("cucumber") ||
                description.contains("peanuts") || description.contains("bacon") ||
                description.contains("green salad") || description.contains("milk") ||
                description.contains("glass") || description.contains("coconut") ||
                description.contains("broccoli") || description.contains("pretzel") ||
                description.contains("avocado") || description.contains("leafy green") ||
                description.contains("bagel") || description.contains("salt") ||
                description.contains("lobster")) {
            addToCategory(emoji.getUnicode(), "FOOD");
            return;
        }

        if (description.contains("building") || description.contains("house") ||
                description.contains("city") || description.contains("mountain") ||
                description.contains("beach") || description.contains("travel") ||
                description.contains("hotel") || description.contains("school") ||
                description.contains("hospital") || description.contains("church") ||
                description.contains("castle") || description.contains("tent") ||
                description.contains("bridge") || description.contains("tower") ||
                description.contains("airport") || description.contains("station") ||
                description.contains("road") || description.contains("map") ||
                description.contains("globe") || description.contains("park") ||
                description.contains("garden") || description.contains("desert") ||
                description.contains("island") || description.contains("volcano") ||
                description.contains("fountain") || description.contains("ferris wheel") ||
                description.contains("roller coaster") || description.contains("stadium") ||
                description.contains("wilderness") || description.contains("camp") ||
                tags.contains("travel") || tags.contains("place") ||
                tags.contains("building") || description.contains("bank") ||
                description.contains("convenience store") || description.contains("wedding") ||
                description.contains("european post office") || description.contains("factory") ||
                description.contains("japan") || description.contains("fuji") ||
                description.contains("statue of liberty") || description.contains("hotel") ||
                description.contains("building") || description.contains("structure") ||
                description.contains("house") || description.contains("silhouette of japan") ||
                description.contains("mount fuji") || description.contains("statue") ||
                description.contains("toilet") || description.contains("shower") ||
                description.contains("bathroom") || description.contains("restroom") ||
                description.contains("door") || description.contains("brick") ||
                description.contains("motorway") || description.contains("railway") ||
                description.contains("railway track") || description.contains("kaaba") ||
                description.contains("mosque") || description.contains("place of worship") ||
                description.contains("bed") || description.contains("bedroom") ||
                description.contains("couch") || description.contains("lamp") ||
                description.contains("shinto shrine") || description.contains("moyai") ||
                description.contains("baggage claim") || description.contains("left luggage") ||
                description.contains("customs") || description.contains("atm") ||
                description.contains("automated teller machine")) {
            addToCategory(emoji.getUnicode(), "PLACES");
            return;
        }

        if (description.contains("sport") || description.contains("game") ||
                description.contains("activity") || description.contains("dance") ||
                description.contains("music") || description.contains("art") ||
                description.contains("ball") || description.contains("medal") ||
                description.contains("run") || description.contains("swim") ||
                description.contains("bike") || description.contains("exercise") ||
                description.contains("movie") || description.contains("camera") ||
                description.contains("paint") || description.contains("instrument") ||
                description.contains("golf") || description.contains("tennis") ||
                description.contains("soccer") || description.contains("basketball") ||
                description.contains("baseball") || description.contains("ski") ||
                description.contains("snowboard") || description.contains("surf") ||
                description.contains("fishing") || description.contains("boxing") ||
                description.contains("martial arts") || description.contains("gymnastics") ||
                description.contains("yoga") || description.contains("hiking") ||
                description.contains("camping") || description.contains("photography") ||
                description.contains("painting") || description.contains("writing") ||
                description.contains("reading") || description.contains("shopping") ||
                description.contains("hobby") || description.contains("play") ||
                description.contains("party") || description.contains("celebration") ||
                description.contains("festival") || description.contains("concert") ||
                description.contains("cinema") || description.contains("theatre") ||
                description.contains("circus") || tags.contains("sport") ||
                tags.contains("activity") || tags.contains("entertainment") ||
                description.contains("briefcase") || description.contains("business") ||
                description.contains("wrapped present") || description.contains("present") ||
                description.contains("gift") || description.contains("fireworks") ||
                description.contains("wind chime") || description.contains("carp streamer") ||
                description.contains("pine decoration") || description.contains("videocassette") ||
                description.contains("optical disc") || description.contains("dvd") ||
                description.contains("minidisc") || description.contains("floppy disk") ||
                description.contains("telephone") || description.contains("phone") ||
                description.contains("mobile") || description.contains("pager") ||
                description.contains("fax") || description.contains("television") ||
                description.contains("radio") || description.contains("bicycle") ||
                description.contains("bicyclist") || description.contains("water polo") ||
                description.contains("swimming") || description.contains("skiing") ||
                description.contains("trophy") || description.contains("award") ||
                description.contains("medal") || description.contains("ticket") ||
                description.contains("clapper board") || description.contains("film") ||
                description.contains("microphone") || description.contains("headphone") ||
                description.contains("violin") || description.contains("saxophone") ||
                description.contains("guitar") || description.contains("billiards") ||
                description.contains("pool") || description.contains("direct hit") ||
                description.contains("joystick") || description.contains("video game") ||
                description.contains("controller") || description.contains("game") ||
                description.contains("recreation") || description.contains("barber pole") ||
                description.contains("studio microphone") || description.contains("slot machine") ||
                description.contains("laptop") || description.contains("computer") ||
                description.contains("desktop") || description.contains("printer") ||
                description.contains("keyboard") || description.contains("shopping") ||
                description.contains("firecracker") || description.contains("red gift") ||
                description.contains("flying disc") || description.contains("jigsaw") ||
                description.contains("puzzle") || description.contains("abacus") ||
                description.contains("book") || description.contains("newspaper") ||
                description.contains("notebook") || description.contains("ledger") ||
                description.contains("scroll") || description.contains("page") ||
                description.contains("memo") || description.contains("calendar") ||
                description.contains("clipboard") || description.contains("pushpin") ||
                description.contains("paperclip") || description.contains("straight ruler") ||
                description.contains("triangular ruler") || description.contains("card index") ||
                description.contains("film frames") || description.contains("admission ticket") ||
                description.contains("label") || description.contains("racing car") ||
                description.contains("racing motorcycle") || description.contains("ice hockey") ||
                description.contains("badminton") || description.contains("weight lifter") ||
                description.contains("goal net") || description.contains("drum") ||
                description.contains("film projector") || description.contains("level slider") ||
                description.contains("control knobs") || description.contains("candle") ||
                description.contains("rolled up") || description.contains("crayon") ||
                description.contains("card index dividers") || description.contains("spiral note") ||
                description.contains("spiral calendar") || description.contains("scooter") ||
                description.contains("motor scooter") || description.contains("canoe") ||
                description.contains("sled") || description.contains("flying saucer") ||
                description.contains("skateboard") || description.contains("curling stone")) {
            addToCategory(emoji.getUnicode(), "ACTIVITIES");
            return;
        }

        // Par défaut, mettre dans "symbols"
        addToCategory(emoji.getUnicode(), "SYMBOLS");
    }

    private void addToCategory(String emoji, String category) {
        emojiCategories.get(category).add(emoji);
    }
}
