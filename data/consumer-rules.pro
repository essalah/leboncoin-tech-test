# Fix: this file was referenced by data/build.gradle.kts's consumerProguardFiles(...) but never
# actually existed in the original project release builds would fail the moment minification
# was ever turned on (exactly what happened when :app's release build type enabled it). Nothing
# app-specific is required here today; this file exists so the reference is valid and so future
# rules have an obvious home.
