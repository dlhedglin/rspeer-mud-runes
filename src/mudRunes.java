import javafx.scene.paint.Stop;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.*;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.chatter.ClanChat;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.listeners.SkillListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.event.types.SkillEvent;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
@ScriptMeta(name = "Mud Runes",  desc = "Script description", developer = "Developer's Name", category = ScriptCategory.MAGIC)
public class mudRunes extends Script implements RenderListener, SkillListener, ChatMessageListener {
    private static String mudRune = "Mud rune";
    private static String pureEss = "Pure essence";
    private static String bindingNeck = "Binding necklace";
    private static String waterTalasman = "Water talisman";
    private static String duelRing = "Ring of dueling(8)";
    private static String strangeFruit = "Strange fruit";
    private static String willowLog = "Willow logs";
    private static Area cWars = Area.rectangular(2435, 3098, 2446, 3080);
    private static Area baloonArea = Area.rectangular(2456, 3110, 2462, 3104);
    private static Area ruins = Area.rectangular(3296, 3481, 3311, 3466);
    private static Area insideRuins = Area.rectangular(2653, 4841, 2663, 4833);
    private StopWatch timer;
    private StopWatch hopTimer;
    private int runesMade;
    private int xpGained;
    private boolean needLogs;
    private int logsInCrate;
    private String muleName = "Hunthound7";
    private String muleKeyword = "Time";
    private boolean muling;
    private boolean allRunesWithdrawn;
    private int stamPrice;
    private int stamDosesDrank;
    private int cost;
    private static final String BASE_URL = "http://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=";

    @Override
    public void onStart()
    {
        timer = StopWatch.start();
        hopTimer = StopWatch.start();
        runesMade = 0;
        xpGained = 0;
        needLogs = true;
        logsInCrate = 0;
        muling = false;
        allRunesWithdrawn = false;
        stamPrice = getPrice(12631);
        stamDosesDrank = 0;
        cost = 0;
    }

    @Override
    public int loop()
    {
        if(!Inventory.contains(pureEss))
        {
            if(cWars.contains(Players.getLocal()))
            {
                if(!muling && Worlds.getCurrent() == 307)
                {
                    randomSafeP2P();
                    Time.sleepUntil(()-> Worlds.getCurrent() != 307, 5000);
                }
                else if(muling)
                {
                    if(Worlds.getCurrent() != 307)
                    {
                        if(Bank.isOpen())
                        {
                            Bank.close();
                            Time.sleepUntil(()-> Bank.isClosed(),5000);
                        }
                        else
                        {
                            WorldHopper.hopTo(307);
                            Time.sleepUntil(()-> Worlds.getCurrent() == 307, 5000);
                        }

                    }
                    else if(allRunesWithdrawn)
                    {
                        if(Bank.isOpen())
                            Bank.close();
                        else if(Trade.isOpen() || Trade.isOpen(true))
                        {
                            if(!Inventory.contains(mudRune))
                            {
                                if(Trade.hasOtherAccepted())
                                {
                                    if(Trade.isOpen(true))
                                    {
                                        Trade.accept();
                                        Time.sleepUntil(()-> !Trade.isOpen(), 5000);
                                        muling = false;
                                        allRunesWithdrawn = false;
                                    }
                                    else
                                    {
                                        Trade.accept();
                                        Time.sleepUntil(()-> Trade.isOpen(true), 3000);
                                    }
                                }
                            }
                            else
                            {
                                Trade.offerAll(mudRune);
                                Time.sleepUntil(()-> Trade.contains(true,mudRune), 3000);
                            }
                        }
                        else
                        {
                            Player mule = Players.getNearest(muleName);
                            if(mule != null)
                            {
                                mule.interact("Trade with");
                                Time.sleepUntil(()-> Trade.isOpen(), 5000);
                            }
                        }

                    }
                    else
                    {
                        if(Bank.isOpen())
                        {
                            Bank.withdrawAll(mudRune);
                            Time.sleepUntil(()-> Bank.getCount(mudRune) == 0, 3000);
                            allRunesWithdrawn = true;
                        }
                        else
                        {
                            Bank.open(BankLocation.CASTLE_WARS);
                            Time.sleepUntil(()-> Bank.isOpen(), 3000);
                        }
                    }
                }
                else if (Bank.isOpen())
                {
                    if(Bank.getWithdrawMode() == Bank.WithdrawMode.NOTE)
                    {
                        Bank.setWithdrawMode(Bank.WithdrawMode.ITEM);
                        Time.sleepUntil(()-> Bank.getWithdrawMode() == Bank.WithdrawMode.ITEM,3000);
                    }
                    else if (Inventory.contains(mudRune))
                    {
                        Bank.depositAll(mudRune);
                        Time.sleepUntil(() -> Inventory.getCount(mudRune) == 0, 3000);
                    }
                    else if(Inventory.contains(willowLog) && logsInCrate != 0)
                    {
                        Bank.depositAll(willowLog);
                    }
                    else if(Inventory.contains("Vial"))
                    {
                        Bank.depositAll("Vial");
                        Time.sleepUntil(()-> Inventory.getCount("Vial") == 0, 3000);
                    }
                    else if (!Equipment.isOccupied(EquipmentSlot.NECK))
                    {
                        if (Inventory.getCount(bindingNeck) == 0 && Bank.getCount(bindingNeck) == 0)
                        {
                            return -1;
                        }
                        else if(Inventory.getCount(bindingNeck) > 0)
                        {
                            Inventory.getFirst(bindingNeck).interact("Wear");
                            Time.sleepUntil(()->Equipment.isOccupied(EquipmentSlot.NECK),3000);
                        }
                        else if(Bank.getCount(bindingNeck) > 0)
                        {
                            Bank.withdraw(bindingNeck,1);
                            Time.sleepUntil(()-> Inventory.getCount(bindingNeck) > 0, 3000);
                        }

                    }
                    else if (!Equipment.isOccupied(EquipmentSlot.RING))
                    {
                        if (Inventory.getCount(duelRing) == 0 && Bank.getCount(duelRing) == 0)
                        {
                            return -1;
                        }
                        else if(Inventory.getCount(duelRing) > 0)
                        {
                            Inventory.getFirst(duelRing).interact("Wear");
                            Time.sleepUntil(()->Equipment.isOccupied(EquipmentSlot.RING),3000);
                        }
                        else if(Bank.getCount(duelRing) > 0)
                        {
                            Bank.withdraw(duelRing,1);
                            Time.sleepUntil(()-> Inventory.getCount(duelRing) > 0, 3000);
                        }

                    }
                    else if(Inventory.getCount(waterTalasman) == 0)
                    {
                        if(Bank.getCount(waterTalasman) == 0)
                            return -1;
                        else {
                            Bank.withdraw(waterTalasman ,1);
                            Time.sleepUntil(()-> Inventory.getCount(waterTalasman) > 0,3000);
                        }
                    }
                    else if(Inventory.getCount(willowLog) == 0 && logsInCrate == 0)
                    {
                        if(Bank.getCount(willowLog) == 0)
                            return -1;
                        else {
                            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
                            Time.sleepUntil(()-> Bank.getWithdrawMode() == Bank.WithdrawMode.NOTE,3000);
                            Bank.withdraw(willowLog,100);
                            Time.sleepUntil(()-> Inventory.getCount(willowLog) > 0,3000);
                        }
                    }
                    else if(Inventory.getCount("Water rune") == 0)
                    {
                        if(Bank.getCount("Water rune") == 0)
                            return -1;
                        else {
                            Bank.withdrawAll("Water rune");
                            Time.sleepUntil(()-> Inventory.getCount("Water rune") > 0,3000);
                        }
                    }
                    else if(!Movement.isStaminaEnhancementActive() && Movement.getRunEnergy() < 60 && (Bank.getFirst(a-> a.getName().contains("Stamina")) != null || haveStaminas())) {
                        if(Inventory.getFirst(a-> a.getName().contains("Stamina")) != null) {
                            Inventory.getFirst(a-> a.getName().contains("Stamina")).interact("Drink");
                            stamDosesDrank += 1;
                            cost += stamPrice;
                            Time.sleepUntil(()-> Movement.isStaminaEnhancementActive(),3000);
                        }
                        else if(Bank.getFirst(a-> a.getName().contains("Stamina")) != null) {
                            Bank.getFirst(a-> a.getName().contains("Stamina")).interact("Withdraw-1");
                            Time.sleepUntil(()-> Inventory.getFirst(a-> a.getName().contains("Stamina")) != null,3000);
                        }
                        else
                            return -1;

                    }
                    else if(haveStaminas())
                    {
                        Bank.deposit(a-> a.getName().contains("Stamina"),1);
                        Time.sleepUntil(()-> !haveStaminas(), 3000);
                    }
                    else if(Movement.getRunEnergy() < 30 && (Bank.getCount(strangeFruit) > 0 || Inventory.getCount(strangeFruit) > 0)) {
                        if(Inventory.getCount(strangeFruit) > 0) {
                            Inventory.getFirst(strangeFruit).interact("Eat");

                        }
                        else if(Bank.getCount(strangeFruit) > 0) {
                            Bank.withdraw(strangeFruit,1);
                            Time.sleepUntil(()-> Inventory.getCount(strangeFruit) > 0, 3000);
                        }
                    }
                    else if(Inventory.getCount(pureEss) == 0) {
                        if(Bank.getCount(pureEss) == 0)
                        {
                            return -1;
                        }
                        else
                        {
                            Bank.withdrawAll(pureEss);
                            Time.sleepUntil(()-> Inventory.contains(pureEss), 3000);
                        }
                    }
                    return Random.nextInt(222,444);
                }
                else
                {
                    Bank.open(BankLocation.CASTLE_WARS);
                    Time.sleepUntil(()-> Bank.isOpen(), 3000);
                }
            }
            else {
                if(Equipment.isOccupied(EquipmentSlot.RING)) {
                    EquipmentSlot.RING.interact("Castle Wars");
                    Time.sleepUntil(()-> cWars.contains(Players.getLocal()),3000);
                }
                else {
                    Log.info("No dueling ring, stopping");
                    return -1;
                }
            }
        }
        else if(nearCwars()){ // near cwars
            if(baloonArea.contains(Players.getLocal()))
            {
                if(EnterInput.isOpen() && logsInCrate == 0)
                {
                    int logsCount = Inventory.getCount(willowLog);
                    EnterInput.initiate(100);
                    logsInCrate = 100;
                }
                else if(logsInCrate == 0)
                {
                    SceneObjects.getNearest("Log storage").interact("Store");
                    Time.sleepUntil(()-> Dialog.isOpen(),3000);
                }
                else if(Interfaces.isOpen(469))
                {
                    Interfaces.getComponent(469,18).interact("Travel");
                    Time.sleepUntil(()-> nearRuins(),5000);
                    logsInCrate --;
                    //Log.info("Logs in storage: " + logsInCrate);
                }
                else
                {
                    Npc ass = Npcs.getNearest("Assistant Marrow");
                    if(ass != null && ass.interact("Fly"))
                    {
                        Time.sleepUntil(()-> Interfaces.isOpen(469) ,3000);
                    }
                }
            }
            else
            {
                if(hopTimer.exceeds(Duration.ofMinutes(30)))
                {
                    if(Bank.isOpen())
                        Bank.close();
                    else
                    {
                        if(randomSafeP2P())
                            hopTimer.reset();
                    }

                }
                else
                {
                    Movement.walkToRandomized(baloonArea.getCenter());
                    toggleRun();
                    Time.sleepUntil(()-> !Players.getLocal().isMoving() || baloonArea.contains(Players.getLocal()), Random.low( 1666, 2222));
                }

            }
        }
        else if(nearRuins()){ // near altar
            SceneObject ruins = SceneObjects.getNearest("Mysterious ruins");
            if(ruins!= null)
            {
                ruins.interact("Enter");
                Time.sleepUntil(()-> insideRuins() || !Players.getLocal().isMoving(), 3000);
            }
        }
        else if(insideRuins()){
            SceneObject earthAltar = SceneObjects.getNearest("Altar");
            if(Skills.getCurrentLevel(Skill.RUNECRAFTING) < 13)
            {
                earthAltar.click();
                Time.sleepUntil(()-> !Inventory.contains(pureEss), 3000);
            }
            else if(Inventory.isItemSelected())
            {
                earthAltar.interact("Use");
                Time.sleepUntil(()-> !Inventory.contains(pureEss), 3000);
            }
            else
            {
                Inventory.getFirst(waterTalasman).interact("Use");
                Time.sleepUntil(()-> Inventory.isItemSelected(),3000);
                int countEss = Inventory.getCount(pureEss);
                earthAltar.interact("Use");
                if(Time.sleepUntil(()-> !Inventory.contains(pureEss), 6000))
                    runesMade += countEss;

            }

        }


        return Random.low(111,555);
    }
    @Override
    public void notify(RenderEvent renderEvent) {
        Graphics g = renderEvent.getSource();
        g.setColor(new Color(0,0,0,150));
        g.fillRoundRect(5,30,150,130,10,10);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.white);
        g.drawString("Runtime: " + timer.toElapsedString(), 10,50);
        g.drawString("Runes made: " + runesMade, 10,70);
        g.drawString("Runes/hr: " + Math.floor(timer.getHourlyRate(runesMade)), 10,90);
        g.drawString("Xp:hr " + xpGained +"/" + Math.floor(timer.getHourlyRate(xpGained)), 10,110);
        g.drawString("Stam Doses/hr "+ stamDosesDrank + "/" + Math.floor(timer.getHourlyRate(stamDosesDrank)), 10,130);
        g.drawString("Costs " + cost, 10,150);


    }
    @Override
    public void notify(SkillEvent skillEvent) {
        if(skillEvent.getType() == SkillEvent.TYPE_EXPERIENCE)
        {
            xpGained += skillEvent.getCurrent() - skillEvent.getPrevious();
        }

    }
    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        if(chatMessageEvent.getType() == ChatMessageType.CLAN_CHANNEL)
        {
            Log.info(chatMessageEvent.getMessage());
            if(chatMessageEvent.getMessage().contains(muleKeyword))
            {
                muling = true;
                Log.info("Muling now true");
            }
        }

    }

    int getPrice(final int id) {

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(BASE_URL + id).openStream()))) {

            final String raw = reader.readLine().replace(",", "").replace("\"", "").split("price:")[1].split("}")[0];

            return raw.endsWith("m") || raw.endsWith("k") ? (int) (Double.parseDouble(raw.substring(0, raw.length() - 1))

                    * (raw.endsWith("m") ? 1000000 : 1000)) : Integer.parseInt(raw);

        } catch (final Exception e) {

            e.printStackTrace();

        }

        return -1;

    }


    private boolean toggleRun()
    {
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > Random.nextInt(10, 30)) { // If our energy is higher than a random value 10-30
            Movement.toggleRun(true); // Toggle run
            return true;
        }
        return false;
    }
    private boolean haveStaminas()
    {
        if(Inventory.getFirst(a-> a.getName().contains("Stamina")) != null)
        {
            return true;
        }
        else
            return false;
    }
    public static boolean randomSafeP2P(){
        if(Bank.isOpen())
        {
            Bank.close();
            Time.sleepUntil(()-> Bank.isClosed(),3000);
        }
        return WorldHopper.randomHop(world -> world.getId() != Game.getClient().getCurrentWorld() && world.isMembers() && !world.isPVP()  && !world.isSkillTotal()
                && !world.isTournament() && !world.isHighRisk()
                && !world.isDeadman() && !world.isSeasonDeadman() && world.getLocation() == RSWorld.LOCATION_US
        ); // Check if world is random/safe/p2p
    }
    private boolean nearCwars()
    {
        return cWars.getCenter().distance(Players.getLocal()) < 50;
    }
    private boolean nearRuins()
    {
        return ruins.getCenter().distance(Players.getLocal()) < 15;
    }
    private boolean insideRuins()
    {
        return insideRuins.getCenter().distance(Players.getLocal()) < 15;
    }

}

