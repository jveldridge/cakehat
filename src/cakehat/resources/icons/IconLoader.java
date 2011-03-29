package cakehat.resources.icons;

import cakehat.Allocator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import support.utils.FileExtensionFilter;

/**
 * Loads an icon of size either 16x16 or 32x32 as specified by {@link IconSize}.
 * The icons are loaded with respect to the package this class is in. The
 * reason for having this class instead of directly loading an icon is so
 * that refactoring of the packages containing the icons does not require
 * changing hard-coded image paths all over the code base.
 *
 * @author jak2
 */
public class IconLoader
{
    /**
     * The size of the icon to be used.
     */
    public static enum IconSize
    {
        /**
         * An icon that is 16 by 16 pixels.
         */
        s16x16("16x16"),
        /**
         * An icon that is 32 by 32 pixels.
         */
        s32x32("32x32");

        /**
         * The name of the directory containing the icons of this size.
         */
        private final String dirName;

        private IconSize(String dirName)
        {
            this.dirName = dirName;
        }
    }

    /**
     * The images that are available to be used as icons.
     */
    public static enum IconImage
    {
        ACCESSORIES_CALCULATOR("accessories-calculator.png"),
        ACCESSORIES_CHARACTER_MAP("accessories-character-map.png"),
        ACCESSORIES_TEXT_EDITOR("accessories-text-editor.png"),
        ADDRESS_BOOK_NEW("address-book-new.png"),
        APPLICATION_CERTIFICATE("application-certificate.png"),
        APPLICATION_X_EXECUTABLE("application-x-executable.png"),
        APPLICATIONS_ACCESSORIES("applications-accessories.png"),
        APPLICATIONS_DEVELOPMENT("applications-development.png"),
        APPLICATIONS_GAMES("applications-games.png"),
        APPLICATIONS_GRAPHICS("applications-graphics.png"),
        APPLICATIONS_INTERNET("applications-internet.png"),
        APPLICATIONS_MULTIMEDIA("applications-multimedia.png"),
        APPLICATIONS_OFFICE("applications-office.png"),
        APPLICATIONS_OTHER("applications-other.png"),
        APPLICATIONS_SYSTEM("applications-system.png"),
        APPOINTMENT_NEW("appointment-new.png"),
        AUDIO_CARD("audio-card.png"),
        AUDIO_INPUT_MICROPHONE("audio-input-microphone.png"),
        AUDIO_VOLUME_HIGH("audio-volume-high.png"),
        AUDIO_VOLUME_LOW("audio-volume-low.png"),
        AUDIO_VOLUME_MEDIUM("audio-volume-medium.png"),
        AUDIO_VOLUME_MUTED("audio-volume-muted.png"),
        AUDIO_X_GENERIC("audio-x-generic.png"),
        BATTERY_CAUTION("battery-caution.png"),
        BATTERY("battery.png"),
        BOOKMARK_NEW("bookmark-new.png"),
        CAMERA_PHOTO("camera-photo.png"),
        CAMERA_VIDEO("camera-video.png"),
        COMPUTER("computer.png"),
        CONTACT_NEW("contact-new.png"),
        DIALOG_ERROR("dialog-error.png"),
        DIALOG_INFORMATION("dialog-information.png"),
        DIALOG_WARNING("dialog-warning.png"),
        DOCUMENT_NEW("document-new.png"),
        DOCUMENT_OPEN("document-open.png"),
        DOCUMENT_PRINT_PREVIEW("document-print-preview.png"),
        DOCUMENT_PRINT("document-print.png"),
        DOCUMENT_PROPERTIES("document-properties.png"),
        DOCUMENT_SAVE_AS("document-save-as.png"),
        DOCUMENT_SAVE("document-save.png"),
        DRIVE_HARDDISK("drive-harddisk.png"),
        DRIVE_OPTICAL("drive-optical.png"),
        DRIVE_REMOVABLE_MEDIA("drive-removable-media.png"),
        EDIT_CLEAR("edit-clear.png"),
        EDIT_COPY("edit-copy.png"),
        EDIT_CUT("edit-cut.png"),
        EDIT_DELETE("edit-delete.png"),
        EDIT_FIND_REPLACE("edit-find-replace.png"),
        EDIT_FIND("edit-find.png"),
        EDIT_PASTE("edit-paste.png"),
        EDIT_REDO("edit-redo.png"),
        EDIT_SELECT_ALL("edit-select-all.png"),
        EDIT_UNDO("edit-undo.png"),
        EMBLEM_FAVORITE("emblem-favorite.png"),
        EMBLEM_IMPORTANT("emblem-important.png"),
        EMBLEM_PHOTOS("emblem-photos.png"),
        EMBLEM_READONLY("emblem-readonly.png"),
        EMBLEM_SYMBOLIC_LINK("emblem-symbolic-link.png"),
        EMBLEM_SYSTEM("emblem-system.png"),
        EMBLEM_UNREADABLE("emblem-unreadable.png"),
        FACE_ANGEL("face-angel.png"),
        FACE_CRYING("face-crying.png"),
        FACE_DEVILISH("face-devilish.png"),
        FACE_GLASSES("face-glasses.png"),
        FACE_GRIN("face-grin.png"),
        FACE_KISS("face-kiss.png"),
        FACE_MONKEY("face-monkey.png"),
        FACE_PLAIN("face-plain.png"),
        FACE_SAD("face-sad.png"),
        FACE_SMILE_BIG("face-smile-big.png"),
        FACE_SMILE("face-smile.png"),
        FACE_SURPRISE("face-surprise.png"),
        FACE_WINK("face-wink.png"),
        FOLDER_DRAG_ACCEPT("folder-drag-accept.png"),
        FOLDER_NEW("folder-new.png"),
        FOLDER_OPEN("folder-open.png"),
        FOLDER_REMOTE("folder-remote.png"),
        FOLDER_SAVED_SEARCH("folder-saved-search.png"),
        FOLDER_VISITING("folder-visiting.png"),
        FOLDER("folder.png"),
        FONT_X_GENERIC("font-x-generic.png"),
        FORMAT_INDENT_LESS("format-indent-less.png"),
        FORMAT_INDENT_MORE("format-indent-more.png"),
        FORMAT_JUSTIFY_CENTER("format-justify-center.png"),
        FORMAT_JUSTIFY_FILL("format-justify-fill.png"),
        FORMAT_JUSTIFY_LEFT("format-justify-left.png"),
        FORMAT_JUSTIFY_RIGHT("format-justify-right.png"),
        FORMAT_TEXT_BOLD("format-text-bold.png"),
        FORMAT_TEXT_ITALIC("format-text-italic.png"),
        FORMAT_TEXT_STRIKETHROUGH("format-text-strikethrough.png"),
        FORMAT_TEXT_UNDERLINE("format-text-underline.png"),
        GO_BOTTOM("go-bottom.png"),
        GO_DOWN("go-down.png"),
        GO_FIRST("go-first.png"),
        GO_HOME("go-home.png"),
        GO_JUMP("go-jump.png"),
        GO_LAST("go-last.png"),
        GO_NEXT("go-next.png"),
        GO_PREVIOUS("go-previous.png"),
        GO_TOP("go-top.png"),
        GO_UP("go-up.png"),
        HELP_BROWSER("help-browser.png"),
        IMAGE_LOADING("image-loading.png"),
        IMAGE_MISSING("image-missing.png"),
        IMAGE_X_GENERIC("image-x-generic.png"),
        INPUT_GAMING("input-gaming.png"),
        INPUT_KEYBOARD("input-keyboard.png"),
        INPUT_MOUSE("input-mouse.png"),
        INTERNET_GROUP_CHAT("internet-group-chat.png"),
        INTERNET_MAIL("internet-mail.png"),
        INTERNET_NEWS_READER("internet-news-reader.png"),
        INTERNET_WEB_BROWSER("internet-web-browser.png"),
        LIST_ADD("list-add.png"),
        LIST_REMOVE("list-remove.png"),
        MAIL_ATTACHMENT("mail-attachment.png"),
        MAIL_FORWARD("mail-forward.png"),
        MAIL_MARK_JUNK("mail-mark-junk.png"),
        MAIL_MARK_NOT_JUNK("mail-mark-not-junk.png"),
        MAIL_MESSAGE_NEW("mail-message-new.png"),
        MAIL_REPLY_ALL("mail-reply-all.png"),
        MAIL_REPLY_SENDER("mail-reply-sender.png"),
        MAIL_SEND_RECEIVE("mail-send-receive.png"),
        MEDIA_EJECT("media-eject.png"),
        MEDIA_FLASH("media-flash.png"),
        MEDIA_FLOPPY("media-floppy.png"),
        MEDIA_OPTICAL("media-optical.png"),
        MEDIA_PLAYBACK_PAUSE("media-playback-pause.png"),
        MEDIA_PLAYBACK_START("media-playback-start.png"),
        MEDIA_PLAYBACK_STOP("media-playback-stop.png"),
        MEDIA_RECORD("media-record.png"),
        MEDIA_SEEK_BACKWARD("media-seek-backward.png"),
        MEDIA_SEEK_FORWARD("media-seek-forward.png"),
        MEDIA_SKIP_BACKWARD("media-skip-backward.png"),
        MEDIA_SKIP_FORWARD("media-skip-forward.png"),
        MULTIMEDIA_PLAYER("multimedia-player.png"),
        NETWORK_ERROR("network-error.png"),
        NETWORK_IDLE("network-idle.png"),
        NETWORK_OFFLINE("network-offline.png"),
        NETWORK_RECEIVE("network-receive.png"),
        NETWORK_SERVER("network-server.png"),
        NETWORK_TRANSMIT_RECEIVE("network-transmit-receive.png"),
        NETWORK_TRANSMIT("network-transmit.png"),
        NETWORK_WIRED("network-wired.png"),
        NETWORK_WIRELESS_ENCRYPTED("network-wireless-encrypted.png"),
        NETWORK_WIRELESS("network-wireless.png"),
        NETWORK_WORKGROUP("network-workgroup.png"),
        OFFICE_CALENDAR("office-calendar.png"),
        PACKAGE_X_GENERIC("package-x-generic.png"),
        PREFERENCES_DESKTOP_ACCESSIBILITY("preferences-desktop-accessibility.png"),
        PREFERENCES_DESKTOP_ASSISTIVE_TECHNOLOGY("preferences-desktop-assistive-technology.png"),
        PREFERENCES_DESKTOP_FONT("preferences-desktop-font.png"),
        PREFERENCES_DESKTOP_KEYBOARD_SHORTCUTS("preferences-desktop-keyboard-shortcuts.png"),
        PREFERENCES_DESKTOP_LOCALE("preferences-desktop-locale.png"),
        PREFERENCES_DESKTOP_MULTIMEDIA("preferences-desktop-multimedia.png"),
        PREFERENCES_DESKTOP_PERIPHERALS("preferences-desktop-peripherals.png"),
        PREFERENCES_DESKTOP_REMOTE_DESKTOP("preferences-desktop-remote-desktop.png"),
        PREFERENCES_DESKTOP_SCREENSAVER("preferences-desktop-screensaver.png"),
        PREFERENCES_DESKTOP_THEME("preferences-desktop-theme.png"),
        PREFERENCES_DESKTOP_WALLPAPER("preferences-desktop-wallpaper.png"),
        PREFERENCES_DESKTOP("preferences-desktop.png"),
        PREFERENCES_SYSTEM_NETWORK_PROXY("preferences-system-network-proxy.png"),
        PREFERENCES_SYSTEM_SESSION("preferences-system-session.png"),
        PREFERENCES_SYSTEM_WINDOWS("preferences-system-windows.png"),
        PREFERENCES_SYSTEM("preferences-system.png"),
        PRINTER_ERROR("printer-error.png"),
        PRINTER("printer.png"),
        PROCESS_STOP("process-stop.png"),
        PROCESS_WORKING("process-working.png"),
        SOFTWARE_UPDATE_AVAILABLE("software-update-available.png"),
        SOFTWARE_UPDATE_URGENT("software-update-urgent.png"),
        START_HERE("start-here.png"),
        SYSTEM_FILE_MANAGER("system-file-manager.png"),
        SYSTEM_INSTALLER("system-installer.png"),
        SYSTEM_LOCK_SCREEN("system-lock-screen.png"),
        SYSTEM_LOG_OUT("system-log-out.png"),
        SYSTEM_SEARCH("system-search.png"),
        SYSTEM_SHUTDOWN("system-shutdown.png"),
        SYSTEM_SOFTWARE_UPDATE("system-software-update.png"),
        SYSTEM_USERS("system-users.png"),
        TAB_NEW("tab-new.png"),
        TEXT_HTML("text-html.png"),
        TEXT_X_GENERIC_TEMPLATE("text-x-generic-template.png"),
        TEXT_X_GENERIC("text-x-generic.png"),
        TEXT_X_SCRIPT("text-x-script.png"),
        USER_DESKTOP("user-desktop.png"),
        USER_HOME("user-home.png"),
        USER_TRASH_FULL("user-trash-full.png"),
        USER_TRASH("user-trash.png"),
        UTILITIES_SYSTEM_MONITOR("utilities-system-monitor.png"),
        UTILITIES_TERMINAL("utilities-terminal.png"),
        VIDEO_DISPLAY("video-display.png"),
        VIDEO_X_GENERIC("video-x-generic.png"),
        VIEW_FULLSCREEN("view-fullscreen.png"),
        VIEW_REFRESH("view-refresh.png"),
        WEATHER_CLEAR_NIGHT("weather-clear-night.png"),
        WEATHER_CLEAR("weather-clear.png"),
        WEATHER_FEW_CLOUDS_NIGHT("weather-few-clouds-night.png"),
        WEATHER_FEW_CLOUDS("weather-few-clouds.png"),
        WEATHER_OVERCAST("weather-overcast.png"),
        WEATHER_SEVERE_ALERT("weather-severe-alert.png"),
        WEATHER_SHOWERS_SCATTERED("weather-showers-scattered.png"),
        WEATHER_SHOWERS("weather-showers.png"),
        WEATHER_SNOW("weather-snow.png"),
        WEATHER_STORM("weather-storm.png"),
        WINDOW_NEW("window-new.png"),
        X_OFFICE_ADDRESS_BOOK("x-office-address-book.png"),
        X_OFFICE_CALENDAR("x-office-calendar.png"),
        X_OFFICE_DOCUMENT_TEMPLATE("x-office-document-template.png"),
        X_OFFICE_DOCUMENT("x-office-document.png"),
        X_OFFICE_DRAWING_TEMPLATE("x-office-drawing-template.png"),
        X_OFFICE_DRAWING("x-office-drawing.png"),
        X_OFFICE_PRESENTATION_TEMPLATE("x-office-presentation-template.png"),
        X_OFFICE_PRESENTATION("x-office-presentation.png"),
        X_OFFICE_SPREADSHEET_TEMPLATE("x-office-spreadsheet-template.png"),
        X_OFFICE_SPREADSHEET("x-office-spreadsheet.png");

        private final String fileName;

        private IconImage(String fileName)
        {
            this.fileName = fileName;
        }
    }

    private static final String ROOT_ICON_DIR =
            "/" + IconLoader.class.getPackage().getName().replace(".", "/");

    public static BufferedImage loadBufferedImage(IconSize size, IconImage image) throws IOException
    {
        return ImageIO.read(getResourceURL(size, image));
    }

    public static Icon loadIcon(IconSize size, IconImage image)
    {
        return new ImageIcon(getResourceURL(size, image));
    }

    private static final URL getResourceURL(IconSize size, IconImage image)
    {
        String resourcePath = ROOT_ICON_DIR + "/" + size.dirName + "/" + image.fileName;
        URL resourceURL = IconLoader.class.getResource(resourcePath);

        return resourceURL;
    }
    
    

    public static void main(String[] args) throws IOException
    {
        generateIconImageEnums();
    }

    /**
     * This method is only intended to be run by a developer to generate source
     * code for the types of {@link IconImage}. This absolutely should not be
     * used anywhere in cakehat during normal execution. (This situation should
     * not arise, but this code will absolutely not work if the code is inside
     * of a jar.)
     * <br/><br/>
     * This method will only generate enum information for files present in all
     * sizes (as specified by {@link IconSize}).
     */
    private static final void generateIconImageEnums() throws IOException
    {
        FileExtensionFilter imgExtFilter = new FileExtensionFilter("png");

        String path = IconLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File iconDir = new File(path, ROOT_ICON_DIR);


        //Generate enum code only for icons that exist in all sizes

        //Map from icon size to file name
        Map<IconSize, List<String>> icons = new HashMap<IconSize, List<String>>();

        for(IconSize size : IconSize.values())
        {
            File iconSizeDir = new File(iconDir, size.dirName);
            List<File> iconSizeFiles = Allocator.getFileSystemUtilities().getFiles(iconSizeDir, imgExtFilter);

            List<String> iconsForSize = new ArrayList<String>();
            for(File iconFile : iconSizeFiles)
            {
                iconsForSize.add(iconFile.getName());
            }

            icons.put(size, iconsForSize);
        }

        //Choose a mapping for a given icon size, then remove any not in others
        List<String> iconsInCommon = icons.values().iterator().next();
        for(List<String> iconsForSize : icons.values())
        {
            for(String iconForSize : iconsForSize)
            {
                if(!iconsInCommon.contains(iconForSize))
                {
                    iconsInCommon.remove(iconForSize);
                }
            }
        }

        for(int i = 0; i < iconsInCommon.size(); i++)
        {
            String filename = iconsInCommon.get(i);

            String enumname = filename.replace(".png", "");
            enumname = enumname.replace("-", "_");
            enumname = enumname.toUpperCase();

            String enumSourceText = enumname + "(\"" + filename + "\")";

            if(i != iconsInCommon.size() - 1)
            {
                enumSourceText += ",";
            }

            System.out.println(enumSourceText);
        }
    }
}