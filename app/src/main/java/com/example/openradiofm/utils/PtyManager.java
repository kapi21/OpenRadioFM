package com.example.openradiofm.utils;

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
     * Helper to get PTY Label (Multilingual support would be here normally, 
     * but we are focusing on Icons as per V5.0 requirements).
     */
     public static String getPtyLabel(int pty) {
         // Placeholder for future expansion
         return "PTY " + pty;
     }
}
