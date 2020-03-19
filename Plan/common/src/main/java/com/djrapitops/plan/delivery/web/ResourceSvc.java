/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.web;

import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.ResourceSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Supplier;

/**
 * ResourceService implementation.
 *
 * @author Rsl1122
 */
@Singleton
public class ResourceSvc implements ResourceService {

    public final Set<Snippet> snippets;
    private final PlanFiles files;
    private final ResourceSettings resourceSettings;
    private final ErrorHandler errorHandler;

    @Inject
    public ResourceSvc(
            PlanFiles files,
            PlanConfig config,
            ErrorHandler errorHandler
    ) {
        this.files = files;
        this.resourceSettings = config.getResourceSettings();
        this.errorHandler = errorHandler;
        this.snippets = new HashSet<>();
    }

    public void register() {
        Holder.set(this);
    }

    @Override
    public WebResource getResource(String pluginName, String fileName, Supplier<WebResource> source) {
        return applySnippets(pluginName, fileName, getTheResource(pluginName, fileName, source));
    }

    private WebResource applySnippets(String pluginName, String fileName, WebResource resource) {
        Map<Position, StringBuilder> byPosition = calculateSnippets(pluginName, fileName);
        if (byPosition.isEmpty()) return resource;

        String html = applySnippets(resource, byPosition);
        return WebResource.create(html);
    }

    private String applySnippets(WebResource resource, Map<Position, StringBuilder> byPosition) {
        String html = resource.asString();
        if (html == null) {
            return "Error: Given resource did not support WebResource#asString method properly and returned 'null'";
        }

        StringBuilder toHead = byPosition.get(Position.HEAD);
        if (toHead != null) {
            html = StringUtils.replaceOnce(html, "</head>", toHead.append("</head>").toString());
        }

        StringBuilder toBody = byPosition.get(Position.BODY);
        if (toBody != null) {
            if (StringUtils.contains(html, "<!-- End of Page Wrapper -->")) {
                html = StringUtils.replaceOnce(html, "<!-- End of Page Wrapper -->", toBody.toString());
            } else {
                html = StringUtils.replaceOnce(html, "<body>", toBody.append("<body>").toString());
            }
        }

        StringBuilder toBodyEnd = byPosition.get(Position.BODY_END);
        if (toBodyEnd != null) {
            html = StringUtils.replaceOnce(html, "<\body>", toBodyEnd.append("<\body>").toString());
        }

        return html;
    }

    private Map<Position, StringBuilder> calculateSnippets(String pluginName, String fileName) {
        Map<Position, StringBuilder> byPosition = new EnumMap<>(Position.class);
        for (Snippet snippet : snippets) {
            if (snippet.matches(pluginName, fileName)) {
                byPosition.computeIfAbsent(snippet.position, k -> new StringBuilder()).append(snippet.content);
            }
        }
        return byPosition;
    }

    public WebResource getTheResource(String pluginName, String fileName, Supplier<WebResource> source) {
        try {
            if (resourceSettings.shouldBeCustomized(pluginName, fileName)) {
                return getOrWriteCustomized(fileName, source);
            }
        } catch (IOException e) {
            errorHandler.log(L.WARN, getClass(), e.getCause());
        }
        // Return original by default
        return source.get();
    }

    public WebResource getOrWriteCustomized(String fileName, Supplier<WebResource> source) throws IOException {
        Optional<Resource> customizedResource = files.getCustomizableResource(fileName);
        if (customizedResource.isPresent()) {
            return readCustomized(customizedResource.get());
        } else {
            return writeCustomized(fileName, source);
        }
    }

    public WebResource readCustomized(Resource customizedResource) throws IOException {
        try {
            return customizedResource.asWebResource();
        } catch (UncheckedIOException readFail) {
            throw readFail.getCause();
        }
    }

    public WebResource writeCustomized(String fileName, Supplier<WebResource> source) throws IOException {
        WebResource original = source.get();
        byte[] bytes = original.asBytes();
        OpenOption[] overwrite = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
        Files.write(files.getCustomizationDirectory().resolve(fileName), bytes, overwrite);
        return original;
    }

    @Override
    public void addScriptsToResource(String pluginName, String fileName, Position position, String... jsSrcs) {
        if (!fileName.endsWith(".html")) {
            throw new IllegalArgumentException("'" + fileName + "' is not a .html file! Only html files can be added to.");
        }
        String snippet = new TextStringBuilder("<script src=\"")
                .appendWithSeparators(jsSrcs, "\"></script><script src=\"")
                .append("\"></script>").build();
        snippets.add(new Snippet(pluginName, fileName, position, snippet));
    }

    @Override
    public void addStylesToResource(String pluginName, String fileName, Position position, String... cssSrcs) {
        if (!fileName.endsWith(".html")) {
            throw new IllegalArgumentException("'" + fileName + "' is not a .html file! Only html files can be added to.");
        }
        String snippet = new TextStringBuilder("<link href=\"")
                .appendWithSeparators(cssSrcs, "\" ref=\"stylesheet\"></link><link href=\"")
                .append("\" ref=\"stylesheet\"></link>").build();
        snippets.add(new Snippet(pluginName, fileName, position, snippet));
    }

    private static class Snippet {
        private final String pluginName;
        private final String fileName;
        private final Position position;
        private final String content;

        public Snippet(String pluginName, String fileName, Position position, String content) {
            this.pluginName = pluginName;
            this.fileName = fileName;
            this.position = position;
            this.content = content;
        }

        public boolean matches(String pluginName, String fileName) {
            return pluginName.equals(this.pluginName) && fileName.equals(this.fileName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Snippet snippet = (Snippet) o;
            return Objects.equals(pluginName, snippet.pluginName) &&
                    Objects.equals(fileName, snippet.fileName) &&
                    position == snippet.position &&
                    Objects.equals(content, snippet.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pluginName, fileName, position, content);
        }
    }
}