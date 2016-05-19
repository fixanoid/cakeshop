package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.jpmorgan.ib.caonpd.ethereum.enterprise")
public class SpringBootApplication {

    public static void main(String[] args) {
        // setup configs
        WebAppInit.setLoggingPath(true);
        String configDir = FileUtils.expandPath(SystemUtils.USER_DIR, "data");
        System.setProperty("eth.config.dir", configDir);

        // extract geth from WAR (if necessary)
        try {
            extractGeth(configDir);
        } catch (IOException e) {
            System.err.println("!!! ERROR: Failed to extract geth from WAR package");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // boot app
        new SpringApplicationBuilder(SpringBootApplication.class)
            .profiles("container", "spring-boot")
            .run(args);
    }

    private static void extractGeth(String configDir) throws IOException {
	    File war = FileUtils.toFile(GethConfigBean.class.getClassLoader().getResource(""));
	    if (!war.toString().endsWith(".war")) {
	        return; // no need to copy
	    }

	    String gethDir = FileUtils.expandPath(configDir,  "geth");
	    System.out.println("Extracting geth to " + gethDir);

	    ZipFile warZip = new ZipFile(war);
	    Enumeration<? extends ZipEntry> entries = warZip.entries();
	    while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String file = zipEntry.getName();
            if (zipEntry.isDirectory() ||  !file.startsWith("WEB-INF/classes/geth")) {
                continue;
            }

            File target = new File(FileUtils.join(configDir, file.substring(16)));
            File targetDir = target.getParentFile();
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            FileUtils.copyInputStreamToFile(warZip.getInputStream(zipEntry), target);
        }

	    System.setProperty("eth.geth.dir", gethDir);
    }

    @Bean
    @Profile("spring-boot")
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.setContextPath("/ethereum-enterprise");
        return tomcat;
    }

}
