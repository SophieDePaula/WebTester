package com.applitools.Commands;

import com.applitools.Utils.Utils;
import com.applitools.eyes.*;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.StitchMode;
import com.beust.jcommander.Parameter;
import com.google.common.base.Strings;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

public abstract class ApplitoolsTest extends SeleniumTest {
    private static final String CURR_VERSION = "0.0.4";
    //Api-key
    @Parameter(names = {"-k", "--apiKey"}, description = "Applitools api key", required = true)
    protected String apiKey;

    //Match level
    @Parameter(names = {"-ml", "--matchLevel"}, description = "Set match level to one of [Strict|Content|Layout], default = Strict")
    protected String matchLevel;

    //Server url
    @Parameter(names = {"-as", "--applitoolsServer"}, description = "Set Applitools server other than the custom")
    protected String serverUrl;

    //App name
    @Parameter(names = {"-an", "--appName"}, description = "Set own application name, default: WebTester")
    protected String appName = "WebTester";

    //Viewport size
    @Parameter(names = {"-vs", "--viewportsize"}, description = "Set consistent viewport-size of the browser, width:height, ie:1000x600")
    protected String viewportSize;

    //Batch params
    @Parameter(names = {"-ba", "--batchName"}, description = "Set batch name to aggregate tests together")
    protected String batchName;

    //Baseline params
    @Parameter(names = {"-bn", "--baselineName"}, description = "Overrides baseline environment name for cross platform tests")
    protected String baselineEnvName;

    //Proxy
    @Parameter(names = {"-px, --proxy"}, description = "Set proxy for Applitools communication")
    protected String proxyURL;

    //Cut provider
    @Parameter(names = {"-ct", "--cut"}, description = "Specify cut-out border around the original screenshot, tob:bottom:left:right, ie: 50:0:0:0")
    protected String cutProvider;

    //Fullpage sceeenshot
    @Parameter(names = {"-df", "--disableFullPage"}, description = "Disable full-page screenshot")
    protected boolean disableFullPageScreenshot = false;

    //CSS scrolling
    @Parameter(names = {"-us", "--userScroll"}, description = "Simulate user scroll so floating items will be notified")
    protected boolean useUserScrolling = false;

    //Hide scrollbars
    @Parameter(names = {"-sb", "--showScrollbars"}, description = "Set to show scrollbars before screenshot")
    protected boolean showScrollbars = true;

    //Wait before screenshots
    @Parameter(names = {"-wb", "--wait"}, description = "Set wait in seconds between screenshots sections")
    protected int waitBeforeScreenshot = 0;

    protected Eyes eyes_;

    public void Init() throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        super.Init();

        eyes_ = new Eyes() {
            @Override
            public String getBaseAgentId() {
                return String.format("Webtester/%s [%s]", CURR_VERSION, super.getBaseAgentId());
            }
        };

        eyes_.setApiKey(apiKey);
        eyes_.setForceFullPageScreenshot(!disableFullPageScreenshot);
        eyes_.setStitchMode(useUserScrolling ? StitchMode.SCROLL : StitchMode.CSS);
        eyes_.setHideScrollbars(!showScrollbars);

        if (!Strings.isNullOrEmpty(serverUrl)) eyes_.setServerUrl(new URI(serverUrl));
        if (!Strings.isNullOrEmpty(proxyURL)) eyes_.setProxy(new ProxySettings(proxyURL));
        if (!Strings.isNullOrEmpty(baselineEnvName)) eyes_.setBaselineEnvName(baselineEnvName);
        if (!Strings.isNullOrEmpty(batchName)) eyes_.setBatch(new BatchInfo(batchName));
        if (waitBeforeScreenshot > 0) eyes_.setWaitBeforeScreenshots(waitBeforeScreenshot * 1000);

        //Match level
        if (!Strings.isNullOrEmpty(matchLevel)) {
            MatchLevel ml = Utils.parseEnum(MatchLevel.class, matchLevel);
            eyes_.setMatchLevel(ml);
        }
        //Viewport size
        if (!Strings.isNullOrEmpty(viewportSize)) {
            String[] dims = viewportSize.split("x");
            if (dims.length != 2)
                throw new InvalidParameterException("Invalid viewport-size, make sure the call is -vs <width>x<height>");
            eyes_.setViewportSize(
                    driver_,
                    new RectangleSize(
                            Integer.parseInt(dims[0]),
                            Integer.parseInt(dims[1])
                    ));
        }
        //Cut provider
        if (!Strings.isNullOrEmpty(cutProvider)) {
            String[] cuts = cutProvider.split(":");
            if (cuts.length != 4)
                throw new InvalidParameterException("Cut parameter (-cut) incorrect, should be in format Top:Bottom:Left,Right");
            eyes_.setImageCut(
                    new FixedCutProvider(
                            Integer.parseInt(cuts[0]),
                            Integer.parseInt(cuts[1]),
                            Integer.parseInt(cuts[2]),
                            Integer.parseInt(cuts[3])));
        }
    }

    protected void printResult(TestResults result, String testName) {
        System.out.printf("[%s] - %s \n", stringStatus(result), testName);
        System.out.printf("See complete results on: %s \n", result.getUrl());
    }

    protected String stringStatus(TestResults result) {
        if (result.isNew()) return "New";
        if (result.isPassed()) return "Passed";
        return "Failed";
    }
}
