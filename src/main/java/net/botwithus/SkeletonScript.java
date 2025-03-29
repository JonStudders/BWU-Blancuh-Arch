package net.botwithus;

import net.botwithus.api.game.hud.inventories.*;
import net.botwithus.api.game.hud.inventories.Inventory;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.*;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.Random;

public class SkeletonScript extends LoopingScript {

    private BotState botState = BotState.DIG;
    private boolean someBool = true;
    private Random random = new Random();

    enum BotState {
        IDLE,
        PORTER_WAIT,
        PORTER,
        DIG,
        INVENTORY_FULL,
    }

    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);
        subscribe(ChatMessageEvent.class, chatMessageEvent -> {
            if (chatMessageEvent.getMessage().contains("have enough inventory")) {
                println("Inventory full");
                setBotState(BotState.INVENTORY_FULL);
            } else if (chatMessageEvent.getMessage().contains("IV has depleted")) {
                println("Portering 2");
                setBotState(BotState.PORTER_WAIT);
            }
        });
    }

    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        this.loopDelay = 100;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000,7000));
            return;
        }
        switch (botState) {
            case PORTER -> {
                Execution.delay(handlePorter(player));
            }
            case PORTER_WAIT -> {
                setBotState(BotState.PORTER);
                Execution.delay(random.nextLong(1200, 1700));
            }
            case DIG -> {
                Execution.delay(handleDig(player));
            }
            case INVENTORY_FULL -> {
                Execution.delay(handleFull(player));
            }
        }
    }
    public long handlePorter(LocalPlayer player) {
        Item pocketItem = Equipment.getItemIn(Equipment.Slot.POCKET);
        println("Checking pocket");
        println("pocket: " + pocketItem);
        if (Backpack.contains("Sign of the porter IV")) {
            println("c ontains porter");
            int slotId = Backpack.getItem("Sign of the porter IV").getSlot();
            println(slotId);
            Backpack.interact(slotId, "Wear");
            Backpack.interact("Sign of the porter IV");
            println("Interacted twice");
        } else {
            println("no poter");
        }
        setBotState(BotState.DIG);
        return random.nextLong(500,600);
    }

    public long handleDig(LocalPlayer player) {

        if (player.getAnimationId() != -1) {
            println("digging");
            return random.nextLong(500,600);
        } else {
            println("not digging excavate");
            SceneObject closestExcavation = SceneObjectQuery.newQuery().option("Excavate").results().nearest();
            closestExcavation.interact("Excavate");
        }
        return random.nextLong(500,600);
    }

    public long handleFull(LocalPlayer player) {
        println("chec k full");
        if (Backpack.isFull()) {
            println("is full");
            if (Backpack.contains("Archaeological soil box")) {
                println("Ccontains soil box");
                Backpack.interact("Archaeological soil box");
            } else {
                println("no box");
                setBotState(BotState.DIG);
            }
        } else {
            println("not full");
            setBotState(BotState.DIG);
        }

        return random.nextLong(500,600);
    }


    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}
