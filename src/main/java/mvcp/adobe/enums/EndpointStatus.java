package mvcp.adobe.enums;
/**
 * Defined Statuses for Service's Endpoints.
 *
 * Descriptions:
 * <ul>
 *     <li>PENDING: All newly initialized Endpoints are pending</li>
 *     <li>ACTIVE: Endpoints that have a sucessful last request or last probe</li>
 *     <li>SUSPENDED: Endpoints that have a failed last request or last probe</li>
 *     <li>BLOCKED: Black listed endpoints (behavior not yet implemented)</li>
 * </ul>
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public enum EndpointStatus {
    PENDING, ACTIVE, SUSPENDED, BLOCKED
}
