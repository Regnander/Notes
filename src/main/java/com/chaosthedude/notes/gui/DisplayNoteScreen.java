package com.chaosthedude.notes.gui;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.notes.Notes;
import com.chaosthedude.notes.config.ConfigHandler;
import com.chaosthedude.notes.note.Note;
import com.chaosthedude.notes.util.StringUtils;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisplayNoteScreen extends Screen {

	private final Screen parentScreen;
	private NotesButton doneButton;
	private NotesButton pinButton;
	private NotesButton editButton;
	private NotesButton deleteButton;
	private NotesButton prevButton;
	private NotesButton nextButton;
	private Note note;
	private int page;
	private List<String> pages;

	public DisplayNoteScreen(Screen parentScreen, Note note) {
		super(new StringTextComponent(note.getTitle()));
		this.parentScreen = parentScreen;
		this.note = note;

		page = 0;
		pages = new ArrayList<String>();
		pages.add("");
	}

	@Override
	public void init() {
		setupButtons();
		setupPages();
	}

	@Override
	public void tick() {
		prevButton.active = page > 0;
		nextButton.active = page < pages.size() - 1;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		drawCenteredString(font, title.getFormattedText(), width / 2 + 60, 15, -1);
		displayNote();

		super.render(mouseX, mouseY, partialTicks);
	}

	public void displayNote() {
		font.drawSplitString(pages.get(page), 160, 40, width - 200, 0xFFFFFF);
	}

	private void setupButtons() {
		editButton = addButton(new NotesButton(10, 40, 110, 20, I18n.format("notes.edit"), (onPress) -> {
			minecraft.displayGuiScreen(new EditNoteScreen(DisplayNoteScreen.this.parentScreen, note));
		}));
		deleteButton = addButton(new NotesButton(10, 65, 110, 20, I18n.format("notes.delete"), (onPress) -> {
			deleteNote();
		}));
		pinButton = addButton(new NotesButton(10, 90, 110, 20, isPinned() ? I18n.format("notes.unpin") : I18n.format("notes.pin"), (onPress) -> {
			togglePin();
			if (isPinned()) {
				minecraft.displayGuiScreen(null);
			}
		}));
		doneButton = addButton(new NotesButton(10, height - 30, 110, 20, I18n.format("gui.done"), (onPress) -> {
			minecraft.displayGuiScreen(parentScreen);
		}));

		prevButton = addButton(new NotesButton(130, height - 30, 20, 20, I18n.format("<"), (onPress) -> {
			if (page > 0) {
				page--;
			}
		}));
		nextButton = addButton(new NotesButton(width - 30, height - 30, 20, 20, I18n.format(">"), (onPress) -> {
			if (page < pages.size() - 1) {
				page++;
			}
		}));
	}

	private void setupPages() {
		if (note != null) {
			final List<String> lines = ConfigHandler.CLIENT.wrapNote.get() ? font.listFormattedStringToWidth(note.getFilteredText(), width - 200) : StringUtils.wrapToWidth(note.getFilteredText(), width - 200);
			pages = new ArrayList<String>();
			int lineCount = 0;
			String page = "";
			for (String line : lines) {
				if (lineCount > 15) {
					pages.add(page);
					page = "";
					lineCount = 0;
				}

				page = page + line + "\n";
				lineCount++;
			}

			if (!page.isEmpty()) {
				pages.add(page);
			}
		}

		if (pages.isEmpty()) {
			pages.add("");
		}
	}

	private boolean isPinned() {
		return note.equals(Notes.pinnedNote);
	}

	private void togglePin() {
		if (isPinned()) {
			Notes.pinnedNote = null;
			pinButton.setMessage(I18n.format("notes.pin"));
		} else {
			Notes.pinnedNote = note;
			pinButton.setMessage(I18n.format("notes.unpin"));
		}
	}

	private void deleteNote() {
		minecraft.displayGuiScreen(new NotesConfirmScreen((result) -> {
			if (result) {
				note.delete();
			}

			DisplayNoteScreen.this.minecraft.displayGuiScreen(parentScreen);
		}, new StringTextComponent(I18n.format("notes.confirmDelete")), new StringTextComponent(note.getTitle())));
	}

}