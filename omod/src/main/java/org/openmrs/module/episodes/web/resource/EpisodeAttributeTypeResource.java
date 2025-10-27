package org.openmrs.module.episodes.web.resource;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.service.EpisodeAttributeTypeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;

import java.util.List;
import java.util.Optional;


@Resource(name = RestConstants.VERSION_1 + "/episode-attribute-type", supportedClass = EpisodeAttributeType.class, supportedOpenmrsVersions = {"2.0.* - 9.*" })
public class EpisodeAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<EpisodeAttributeType> {

    public static final String SERVER_ERROR_CAN_NOT_FIND_SERVICE = "Can not perform operation. Please contact your system administrator.";

    public EpisodeAttributeTypeResource() {
    }

    private EpisodeAttributeTypeService service() {
        List<EpisodeAttributeTypeService> registeredComponents = Context.getRegisteredComponents(EpisodeAttributeTypeService.class);
        if (!registeredComponents.isEmpty()) {
            return registeredComponents.get(0);
        }
        return null;
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(String)
     */
    @Override
    public EpisodeAttributeType getByUniqueId(String uniqueId) {
        EpisodeAttributeTypeService episodeAttributeTypeService = Optional.ofNullable(service()).orElseThrow(() -> new APIException(SERVER_ERROR_CAN_NOT_FIND_SERVICE));
        return episodeAttributeTypeService.getAttributeTypeByUuid(uniqueId);
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(RequestContext)
     */
    @Override
    protected NeedsPaging<EpisodeAttributeType> doGetAll(RequestContext context) throws ResponseException {
        EpisodeAttributeTypeService episodeAttributeTypeService = Optional.ofNullable(service()).orElseThrow(() -> new APIException(SERVER_ERROR_CAN_NOT_FIND_SERVICE));
        return new NeedsPaging<>(episodeAttributeTypeService.getAllAttributeTypes(false), context);
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
     */
    @Override
    public EpisodeAttributeType newDelegate() {
        return new EpisodeAttributeType();
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(Object)
     */
    @Override
    public EpisodeAttributeType save(EpisodeAttributeType delegate) {
        EpisodeAttributeTypeService episodeAttributeTypeService = Optional.ofNullable(service()).orElseThrow(() -> new APIException(SERVER_ERROR_CAN_NOT_FIND_SERVICE));
        return episodeAttributeTypeService.save(delegate);
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(Object,
     *      RequestContext)
     */
    @Override
    public void purge(EpisodeAttributeType delegate, RequestContext context) throws ResponseException {
        EpisodeAttributeTypeService episodeAttributeTypeService = Optional.ofNullable(service()).orElseThrow(() -> new APIException(SERVER_ERROR_CAN_NOT_FIND_SERVICE));
        episodeAttributeTypeService.retire(delegate, "user action");
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
     */
    @Override
    protected NeedsPaging<EpisodeAttributeType> doSearch(RequestContext context) {
        EpisodeAttributeTypeService episodeAttributeTypeService = Optional.ofNullable(service()).orElseThrow(() -> new APIException(SERVER_ERROR_CAN_NOT_FIND_SERVICE));
        return new NeedsPaging<>(episodeAttributeTypeService.getAttributesByName(context.getParameter("q")), context);
    }

    @Override
    public String getResourceVersion() {
        return "2.0";
    }
}
