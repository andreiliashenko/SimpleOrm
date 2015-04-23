package com.anli.simpleorm.controller.extensions;

import com.anli.simpleorm.controller.EntityController;

public interface RichEntityController extends EntityController {

    FinderExtension getFinder();

    KeyFinderExtension getKeyFinder();
}
