/*
 This file is part of Libresonic.

 Libresonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Libresonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2016 (C) Libresonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.controller;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.libresonic.player.Logger;
import org.libresonic.player.service.SecurityService;
import org.libresonic.player.service.SettingsService;
import org.libresonic.player.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the help page.
 *
 * @author Sindre Mehus
 */
@Controller
@RequestMapping("/help")
public class HelpController {

    private static final Logger logger = Logger.getLogger(HelpController.class);

    private static final int LOG_LINES_TO_SHOW = 50;

    @Autowired
    private VersionService versionService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private SecurityService securityService;

    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<>();

        if (versionService.isNewFinalVersionAvailable()) {
            map.put("newVersionAvailable", true);
            map.put("latestVersion", versionService.getLatestFinalVersion());
        } else if (versionService.isNewBetaVersionAvailable()) {
            map.put("newVersionAvailable", true);
            map.put("latestVersion", versionService.getLatestBetaVersion());
        }

        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();

        String serverInfo = request.getSession().getServletContext().getServerInfo() +
                            ", java " + System.getProperty("java.version") +
                            ", " + System.getProperty("os.name");

        map.put("user", securityService.getCurrentUser(request));
        map.put("brand", settingsService.getBrand());
        map.put("localVersion", versionService.getLocalVersion());
        map.put("buildDate", versionService.getLocalBuildDate());
        map.put("buildNumber", versionService.getLocalBuildNumber());
        map.put("serverInfo", serverInfo);
        map.put("usedMemory", totalMemory - freeMemory);
        map.put("totalMemory", totalMemory);
        File logFile = SettingsService.getLogFile();
        List<String> latestLogEntries = getLatestLogEntries(logFile);
        map.put("logEntries", latestLogEntries);
        map.put("logFile", logFile);

        return new ModelAndView("help","model",map);
    }

    private static List<String> getLatestLogEntries(File logFile) {
        try {
            List<String> lines = new ArrayList<>(LOG_LINES_TO_SHOW);
            ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile, Charset.defaultCharset());
            String current;
            while((current = reader.readLine()) != null && lines.size() < LOG_LINES_TO_SHOW) {
                lines.add(0, current);
            }
            return lines;
        } catch (Exception e) {
            logger.warn("Could not open log file " + logFile, e);
            return null;
        }
    }


}
