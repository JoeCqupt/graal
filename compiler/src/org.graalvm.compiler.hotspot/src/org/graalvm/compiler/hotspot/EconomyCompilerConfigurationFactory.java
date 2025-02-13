/*
 * Copyright (c) 2015, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.compiler.hotspot;

import org.graalvm.compiler.core.Instrumentation;
import org.graalvm.compiler.core.phases.EconomyCompilerConfiguration;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.tiers.CompilerConfiguration;
import org.graalvm.compiler.serviceprovider.ServiceProvider;

/**
 * Factory that creates a {@link EconomyCompilerConfiguration}.
 */
@ServiceProvider(CompilerConfigurationFactory.class)
public class EconomyCompilerConfigurationFactory extends CompilerConfigurationFactory {

    public static final String NAME = "economy";

    public static final int AUTO_SELECTION_PRIORITY = 1;

    public EconomyCompilerConfigurationFactory() {
        this(AUTO_SELECTION_PRIORITY);
    }

    protected EconomyCompilerConfigurationFactory(int autoSelectionPriority) {
        super(NAME, autoSelectionPriority);
    }

    @Override
    public CompilerConfiguration createCompilerConfiguration() {
        return new EconomyCompilerConfiguration();
    }

    @Override
    public BackendMap createBackendMap() {
        // the economy configuration only differs in the frontend, it reuses the "community" backend
        return new DefaultBackendMap(CommunityCompilerConfigurationFactory.NAME);
    }

    @Override
    public Instrumentation createInstrumentation(OptionValues options) {
        return new DefaultInstrumentation();
    }
}
