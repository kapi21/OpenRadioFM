package com.example.openradiofm.utils;

import android.content.Context;
import com.example.openradiofm.R;

public class PtyManager {

    /**
     * Maps RDS PTY code (0-31) to a drawable resource ID.
     * Based on standard RDS/RBDS PTY codes.
     */
    public static int getPtyIconResource(int pty) {
        switch (pty) {
            case 1: // News
            case 2: // Current Affairs
            case 3: // Information
            case 17: // Finance
                return R.drawable.ic_pty_news;

            case 4: // Sport
                return R.drawable.ic_pty_sport;

            case 5: // Education
            case 8: // Science
            case 9: // Varied
            case 18: // Children
            case 19: // Social
            case 20: // Religion
            case 21: // Phone In
            case 22: // Travel
            case 23: // Leisure
            case 29: // Documentary
                return R.drawable.ic_pty_talk;

            case 6: // Drama
            case 7: // Culture
                return R.drawable.ic_pty_culture;

            case 10: // Pop Music
            case 11: // Rock Music
            case 12: // Easy Listening
            case 15: // Other Music
            case 25: // Country
            case 26: // National Music
            case 27: // Oldies
            case 28: // Folk
                return R.drawable.ic_pty_music;

            case 13: // Light Classical
            case 14: // Serious Classical
                return R.drawable.ic_pty_classical;

            case 16: // Weather
                return R.drawable.ic_pty_weather;

            case 24: // Jazz
                return R.drawable.ic_pty_jazz;

            case 30: // Alarm Test
            case 31: // Alarm
                return R.drawable.ic_pty_emergency;

            case 0: // No PTY
            default:
                return 0; // No Icon
        }
    }

    /**
     * Maps RDS PTY Label (String) to a drawable resource ID.
     * Use this when raw integer code is not available.
     * Supports basic keywords in EN, ES, RU.
     */
    public static int getPtyIconResource(String pty) {
        if (pty == null || pty.isEmpty()) return 0;
        
        String cleanPty = pty.toUpperCase(java.util.Locale.US).trim();
        
        // News / Info
        if (cleanPty.contains("NEWS") || cleanPty.contains("NOTICIAS") || cleanPty.contains("INFO") || 
            cleanPty.contains("NOVOSTI")) {
            return R.drawable.ic_pty_news;
        }
        
        // Sport
        if (cleanPty.contains("SPORT") || cleanPty.contains("DEPORTE")) {
            return R.drawable.ic_pty_sport;
        }
        
        // Talk (Educ, Science, Religion, PhoneIn, Travel, Leisure)
        if (cleanPty.contains("TALK") || cleanPty.contains("HABLAR") || cleanPty.contains("CHARLA") ||
            cleanPty.contains("EDU") || cleanPty.contains("SCI") || cleanPty.contains("RELIG") ||
            cleanPty.contains("PHONE") || cleanPty.contains("TRAVEL") || cleanPty.contains("VIAJE") ||
            cleanPty.contains("LEISURE") || cleanPty.contains("OCIO")) {
            return R.drawable.ic_pty_talk;
        }
        
        // Culture / Drama
        if (cleanPty.contains("CULT") || cleanPty.contains("DRAMA") || cleanPty.contains("THEATRE") || 
            cleanPty.contains("TEATRO")) {
            return R.drawable.ic_pty_culture;
        }
        
        // Weather
        if (cleanPty.contains("WEATHER") || cleanPty.contains("TIEMPO") || cleanPty.contains("METEO")) {
            return R.drawable.ic_pty_weather;
        }
        
        // Emergency
        if (cleanPty.contains("ALARM") || cleanPty.contains("EMERG") || cleanPty.contains("TEST")) {
            return R.drawable.ic_pty_emergency;
        }

        // Specific Music Genres
        if (cleanPty.contains("JAZZ")) {
            return R.drawable.ic_pty_jazz;
        }
        
        if (cleanPty.contains("CLASSIC") || cleanPty.contains("CLASICA")) {
            return R.drawable.ic_pty_classical;
        }
        
        // General Music (Pop, Rock, Easy, Top 40, Oldies, Country, Folk)
        if (cleanPty.contains("POP") || cleanPty.contains("ROCK") || cleanPty.contains("MUSIC") || 
            cleanPty.contains("MUSICA") || cleanPty.contains("EASY") || cleanPty.contains("HITS") || 
            cleanPty.contains("OLDIES") || cleanPty.contains("COUNTRY") || cleanPty.contains("FOLK") || 
            cleanPty.contains("NATION") || cleanPty.contains("NACIONAL")) {
            return R.drawable.ic_pty_music;
        }

        return 0; // Default / Unknown
    }

    /**
     * Returns the string resource ID for a PTY code (0-31).
     * Use with context.getString() for localized labels.
     */
    public static int getPtyLabelResId(int pty) {
        switch (pty) {
            case 1: return R.string.pty_news;
            case 2: return R.string.pty_current_affairs;
            case 3: return R.string.pty_information;
            case 4: return R.string.pty_sport;
            case 5: return R.string.pty_education;
            case 6: return R.string.pty_drama;
            case 7: return R.string.pty_culture;
            case 8: return R.string.pty_science;
            case 9: return R.string.pty_varied;
            case 10: return R.string.pty_pop;
            case 11: return R.string.pty_rock;
            case 12: return R.string.pty_easy_listening;
            case 13: return R.string.pty_light_classical;
            case 14: return R.string.pty_serious_classical;
            case 15: return R.string.pty_other_music;
            case 16: return R.string.pty_weather;
            case 17: return R.string.pty_finance;
            case 18: return R.string.pty_children;
            case 19: return R.string.pty_social;
            case 20: return R.string.pty_religion;
            case 21: return R.string.pty_phone_in;
            case 22: return R.string.pty_travel;
            case 23: return R.string.pty_leisure;
            case 24: return R.string.pty_jazz;
            case 25: return R.string.pty_country;
            case 26: return R.string.pty_national;
            case 27: return R.string.pty_oldies;
            case 28: return R.string.pty_folk;
            case 29: return R.string.pty_documentary;
            case 30: return R.string.pty_alarm_test;
            case 31: return R.string.pty_alarm;
            case 0:
            default: return R.string.pty_none;
        }
    }

    /**
     * Returns localized PTY label for display.
     * @param context Android context for string resources
     * @param pty PTY code (0-31)
     */
    public static String getPtyLabel(Context context, int pty) {
        int resId = getPtyLabelResId(pty);
        return context.getString(resId);
    }

    /**
     * Legacy method - returns non-localized label.
     * @deprecated Use getPtyLabel(Context, int) instead.
     */
    @Deprecated
    public static String getPtyLabel(int pty) {
        return "PTY " + pty;
    }
}

