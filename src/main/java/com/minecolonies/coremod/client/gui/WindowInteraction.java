package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.Box;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.util.text.NonSiblingFormattingTextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the citizen.
 */
public class WindowInteraction extends AbstractWindowSkeleton
{
    /**
     * Response buttons default id, gets the response index added to the end 1 to x
     */
    public static final String BUTTON_RESPONSE_ID = "response_";

    /**
     * The citizenData.View object.
     */
    private final ICitizenDataView citizen;

    /**
     * The current interactions.
     */
    private final List<IInteractionResponseHandler> interactions;

    /**
     * The current interaction in the list.
     */
    private int currentInteraction = 0;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public WindowInteraction(final ICitizenDataView citizen)
    {
        super(Constants.MOD_ID + INTERACTION_RESOURCE_SUFFIX, new WindowCitizen(citizen));
        this.citizen = citizen;
        this.interactions = citizen.getOrderedInteractions();
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        setupInteraction();
    }

    /**
     * Setup the current interaction.
     */
    private void setupInteraction()
    {
        if (currentInteraction >= interactions.size())
        {
            close();
            return;
        }

        final IInteractionResponseHandler handler = interactions.get(currentInteraction);
        final Box group = findPaneOfTypeByID(RESPONSE_BOX_ID, Box.class);
        int y = 0;
        int x = 0;
        findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setTextAlignment(Alignment.TOP_LEFT);
        findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setAlignment(Alignment.TOP_LEFT);
        findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setTextContent(citizen.getName() + ": " + handler.getInquiry().getString());
        int responseIndex = 1;
        for (final ITextComponent component : handler.getPossibleResponses())
        {
            final ButtonImage button = new ButtonImage();
            button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
            button.setLabel((IFormattableTextComponent) component);
            button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
            button.setTextColor(SLIGHTLY_BLUE);
            button.setPosition(x, y);
            button.setID(BUTTON_RESPONSE_ID + responseIndex);
            group.addChild(button);

            y += button.getHeight();
            if (y + button.getHeight() >= group.getHeight())
            {
                y = 0;
                x += BUTTON_HEIGHT + BUTTON_BUFFER + button.getWidth();
            }
            responseIndex++;
        }

        handler.onWindowOpened(this, citizen);
    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (!interactions.isEmpty())
        {
            final IInteractionResponseHandler handler = interactions.get(currentInteraction);
            for (final ITextComponent component : handler.getPossibleResponses())
            {
                if (component.getString().equals(button.getLabel()))
                {
                    if (handler.onClientResponseTriggered(component, Minecraft.getInstance().player, citizen, this))
                    {
                        currentInteraction++;
                        setupInteraction();
                        return;
                    }
                }
            }
        }
    }
}
