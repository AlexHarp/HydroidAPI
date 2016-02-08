package au.gov.ga.hydroid;

import au.gov.ga.hydroid.service.EnhancerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
public class EnhanherServiceTest {

   @Autowired
   private EnhancerService enhancerService;

   @Test
   public void testEnhance() throws Exception {
      enhancerService.enhance("default", "Document Title",
            "The polar bear (Ursus maritimus) is a carnivorous bear whose native range lies largely within the Arctic Circle, encompassing the Arctic Ocean, its surrounding seas and surrounding land masses. It is a large bear, approximately the same size as the omnivorous Kodiak bear (Ursus arctos middendorffi).[3] A boar (adult male) weighs around 350–700 kg (772–1,543 lb),[4] while a sow (adult female) is about half that size. Although it is the sister species of the brown bear,[5] it has evolved to occupy a narrower ecological niche, with many body characteristics adapted for cold temperatures, for moving across snow, ice, and open water, and for hunting seals, which make up most of its diet.[6] Although most polar bears are born on land, they spend most of their time on the sea ice. Their scientific name means \"maritime bear\", and derives from this fact. Polar bears hunt their preferred food of seals from the edge of sea ice, often living off fat reserves when no sea ice is present. Because of their dependence on the sea ice, polar bears are classified as marine mammals.[7]\n" +
                  "\n" +
                  "Because of expected habitat loss caused by climate change, the polar bear is classified as a vulnerable species, and at least three of the nineteen polar bear subpopulations are currently in decline.[8] For decades, large-scale hunting raised international concern for the future of the species but populations rebounded after controls and quotas began to take effect.[9] For thousands of years, the polar bear has been a key figure in the material, spiritual, and cultural life of Arctic indigenous peoples, and polar bears remain important in their cultures.",
            "hydroid");
   }

}
