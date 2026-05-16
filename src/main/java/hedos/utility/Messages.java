package hedos.utility;

import java.util.MissingResourceException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ListResourceBundle;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Singleton;

@Singleton
public class Messages {
    private static final Logger logger = LoggerFactory.getLogger(Messages.class);
    private final EventBus eventBus;
    private ResourceBundle resourceBundle = null;
    private Locale currentLocale;

    @Inject
    public Messages(EventBus eventBus) {
        this.eventBus = eventBus;
        // Initialize with default locale (English) when the singleton is created by Guice
        setLocale(Language.ENGLISH);
    }

    public void setLocale(String languageCode) {
        ClassLoader loader = getClass().getClassLoader();
        String requestedLanguage = (languageCode != null) ? languageCode : Language.ENGLISH;
        currentLocale = new Locale(requestedLanguage);
        String baseName = "hedos.utility.messages"; // Fully qualified base name

        logger.info("Attempting to load ResourceBundle for locale '{}' with base name '{}'", currentLocale, baseName);
        
        try {
            // Attempt 1: Load requested locale
            resourceBundle = ResourceBundle.getBundle(baseName, currentLocale, loader);
            logger.info("Successfully loaded ResourceBundle for locale '{}'", currentLocale);
        } catch (MissingResourceException e1) {
            try {
                // Attempt 2: Fallback to English if requested locale failed
                logger.warn("ResourceBundle for locale '{}' not found. Falling back to English (Locale.ENGLISH).", currentLocale);
                resourceBundle = ResourceBundle.getBundle(baseName, Locale.ENGLISH, loader);
                currentLocale = Locale.ENGLISH; // Update currentLocale to reflect actual loaded locale
                logger.info("Successfully loaded fallback ResourceBundle for English.");
            } catch (MissingResourceException e2) {
                // Attempt 3: Final fallback to an empty bundle to prevent NPEs
                logger.error("No ResourceBundles found for base name '{}' or fallback. Labels will be keys only.", baseName, e2);
                resourceBundle = new ListResourceBundle() {
                    @Override
                    protected Object[][] getContents() {
                        return new Object[0][0];
                    }
                };
                currentLocale = Locale.ROOT; // Indicate no specific locale loaded
            }
        }
        eventBus.publish(new EventBus.LocaleChangedEvent(requestedLanguage));
    }

    public String getString(String key) {
        if (resourceBundle == null) {
            logger.warn("ResourceBundle is null. Returning key as-is for key: {}", key);
            return '!' + key + '!';
        }
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            logger.warn("Key '{}' not found in ResourceBundle for locale '{}'. Returning key as-is.", key, currentLocale);
            return '!' + key + '!';
        }
    }

    public final static class Language {
        public final static String ENGLISH = "en";
        public final static String TURKISH = "tr";
    }
}
