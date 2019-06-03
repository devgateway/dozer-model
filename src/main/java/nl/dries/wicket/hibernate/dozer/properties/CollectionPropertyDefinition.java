package nl.dries.wicket.hibernate.dozer.properties;

import nl.dries.wicket.hibernate.dozer.helper.HibernateCollectionType;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;

import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;

/**
 * Collecition property definition
 * 
 * @author dries
 */
public class CollectionPropertyDefinition extends AbstractPropertyDefinition
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Collection type */
	private final HibernateCollectionType type;

	/**
	 * Construct
	 * 
	 * @param owner
	 *            the property owner
	 * @param property
	 *            the name of the field
	 * @param modelCallback
	 *            the {@link ModelCallback}
	 * @param type
	 *            {@link HibernateCollectionType}
	 */
	public CollectionPropertyDefinition(Object owner, String property, ModelCallback modelCallback,
		HibernateCollectionType type)
	{
		super(owner, property, modelCallback);
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public HibernateCollectionType getCollectionType()
	{
		return type;
	}

	/**
	 * @return role property (for a collection)
	 */
	public String getRole()
	{
		return getPropertyOwnerClass(getOwner().getClass()) + "." + getProperty();
	}

	/**
	 * Determines the owner of the current property (could be a superclass of the current class).
	 * For {@link MappedSuperclass} classes, there is no entity registration in hibernate. Therefore trying to
	 * get a collection persister from a {@link MappedSuperclass} will fail. We must retain the first superclass
	 * that is NOT a {@link MappedSuperclass} , which would automatically  be registered by hibernate as an Entity
	 *
	 * 
	 * @param clazz
	 *            {@link Class}
	 * @param parentEntityClazz the parent class, when getPropertyOwnerClass is recursive. This helps retain the
	 *                          first parent which is not {@link MappedSuperclass}
	 * @return found ownen (class name)
	 */
	private String getPropertyOwnerClass(Class<?> parentEntityClazz, Class<?> clazz) {
		Class<?> entityClass = clazz;
		if (clazz.isAnnotationPresent(MappedSuperclass.class)) {
			if (parentEntityClazz == null) {
				throw new RuntimeException("MappedSuperclass without a parent entity is not allowed!");
			}
			entityClass = parentEntityClazz;
		}
		for (Field field : clazz.getDeclaredFields()) {
			if (getProperty().equals(field.getName())) {
				return entityClass.getName();
			}
		}

		return getPropertyOwnerClass(
				clazz.isAnnotationPresent(MappedSuperclass.class) ? parentEntityClazz : clazz,
				clazz.getSuperclass()
		);
	}

	/**
	 * @see #getPropertyOwnerClass(Class, Class)
	 *
	 * @param clazz
	 * @return
	 */
	private String getPropertyOwnerClass(Class<?> clazz) {
		return getPropertyOwnerClass(null, clazz);
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition#getPropertyType()
	 */
	@Override
	public Class<?> getPropertyType()
	{
		return getCollectionType().getPlainInterface();
	}
}
