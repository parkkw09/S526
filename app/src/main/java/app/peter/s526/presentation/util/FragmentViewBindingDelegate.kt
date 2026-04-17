package app.peter.s526.presentation.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Fragment 의 viewLifecycle 에 자동으로 결합되는 ViewBinding delegate.
 * onDestroyView 시점에 참조를 해제하여 메모리 누수를 방지한다.
 *
 * ```
 * class MyFragment : Fragment(R.layout.my_fragment) {
 *     private val binding by viewBinding(MyFragmentBinding::bind)
 * }
 * ```
 */
class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val viewBindingFactory: (android.view.View) -> T,
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    binding = null
                }
            })
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val currentBinding = binding
        if (currentBinding != null) return currentBinding

        val view = thisRef.requireView()
        return viewBindingFactory(view).also { binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(
    viewBindingFactory: (android.view.View) -> T,
): FragmentViewBindingDelegate<T> = FragmentViewBindingDelegate(this, viewBindingFactory)
