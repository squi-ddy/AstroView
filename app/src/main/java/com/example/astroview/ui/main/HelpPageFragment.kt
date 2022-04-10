package com.example.astroview.ui.main

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.astroview.R
import com.example.astroview.api.API
import com.example.astroview.api.CallbackAction
import com.example.astroview.api.data.PageReview
import com.example.astroview.api.data.UserReview
import com.example.astroview.api.handleReturn
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.FragmentHelpPageBinding
import com.example.astroview.ui.main.adapters.PageCommentAdapter
import com.google.android.material.snackbar.Snackbar
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import okhttp3.ResponseBody


class HelpPageFragment : Fragment() {
    private var _binding: FragmentHelpPageBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AppViewModel>()
    private val arguments by navArgs<HelpPageFragmentArgs>()

    companion object {
        private const val CHANNEL_ID = "com.example.astroview"
        private var currId = 1337
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).fab?.hide()
        setHasOptionsMenu(true)

        val markWon = Markwon.builder(requireContext()).apply {
            usePlugin(TablePlugin.create(requireContext()))
            usePlugin(StrikethroughPlugin.create())
        }.build()

        API.client.downloadMarkdown(arguments.pageNumber)
            .handleReturn(object : CallbackAction<ResponseBody> {
                override fun onSuccess(result: ResponseBody, code: Int) {
                    val stream = result.string()
                    requireActivity().runOnUiThread {
                        markWon.setMarkdown(binding.markdownDisplay, stream)
                    }
                }

                override fun onFailure(error: Throwable) {
                    Snackbar.make(
                        binding.root,
                        "Network error. Try again later.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

            })

        binding.helpComments.layoutManager = LinearLayoutManager(requireContext())

        API.client.getPageReviews(arguments.pageNumber).handleReturn(object :
            CallbackAction<List<PageReview>> {
            override fun onSuccess(result: List<PageReview>, code: Int) {
                requireActivity().runOnUiThread {
                    binding.helpComments.adapter = PageCommentAdapter(result)
                }
            }

            override fun onFailure(error: Throwable) {
                Snackbar.make(
                    binding.root,
                    "Network error. Try again later.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })

        binding.helpCommentButton.setOnClickListener {
            val content = binding.helpCommentTextEntry.text.toString()
            if (content.isBlank()) {
                Snackbar.make(
                    binding.root,
                    "Enter some content!",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val auth = viewModel.credentials.auth
                if (auth == null) {
                    Snackbar.make(
                        binding.root,
                        "Please log in first!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                API.client.postPageReview(arguments.pageNumber, auth, UserReview(content))
                    .handleReturn(object :
                        CallbackAction<PageReview> {
                        override fun onSuccess(result: PageReview, code: Int) {
                            Snackbar.make(
                                binding.root,
                                "Comment posted!",
                                Snackbar.LENGTH_SHORT
                            ).show()

                            val t = this

                            API.client.getPageReviews(arguments.pageNumber)
                                .handleReturn(object :
                                    CallbackAction<List<PageReview>> {
                                    override fun onSuccess(result: List<PageReview>, code: Int) {
                                        requireActivity().runOnUiThread {
                                            binding.helpComments.adapter =
                                                PageCommentAdapter(result)
                                        }
                                    }

                                    override fun onFailure(error: Throwable) {
                                        t.onFailure(error)
                                    }
                                })
                        }

                        override fun onFailure(error: Throwable) {
                            Snackbar.make(
                                binding.root,
                                "Network error. Try again later.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val lockGyro = menu.findItem(R.id.menu_gyro_lock)
        lockGyro.isVisible = false

        val help = menu.findItem(R.id.menu_help)
        help.isVisible = false

        val share = menu.findItem(R.id.menu_share)
        share.isVisible = false

        val download = menu.findItem(R.id.menu_download)
        download.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_download -> {
                val request = DownloadManager.Request(
                    API.getDownloadMarkdownLink(arguments.pageNumber).toUri()
                ).apply {
                    setTitle("${arguments.pageName}.md")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setMimeType("text/markdown")
                }
                val manager =
                    requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)

                sendNotification(
                    "info",
                    "The help page is a Markdown file. You must use tools to open the file properly.",
                    null,
                    null
                )

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    private fun sendNotification(
        summary: String,
        message: String,
        group: String?,
        isSummary: Boolean?
    ) {
        createNotificationChannel()
        var builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sextant)
            .setContentTitle(summary)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (group != null && isSummary != null) {
            builder = builder.setGroup(group).setGroupSummary(isSummary)
        }
        NotificationManagerCompat.from(requireContext()).notify(currId, builder.build())
        currId++
    }
}